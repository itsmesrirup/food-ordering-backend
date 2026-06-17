package com.dass.foodordering.food_ordering_backend.service;

import com.dass.foodordering.food_ordering_backend.dto.ai.MenuImportResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Service
public class AiMenuParserService {

    @Value("${openai.api.key:missing-key}")
    private String openaiApiKey;

    private final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MenuImportResult parseMenuImage(MultipartFile file) throws IOException {
        String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
        String imageUrl = "data:image/jpeg;base64," + base64Image;

        // --- REFINED PROMPT FOR BETTER HIERARCHY DETECTION ---
        String prompt = "You are a menu extraction API. Your job is to turn this menu image into structured JSON data.\n" +
                
                "HIERARCHY DETECTION:\n" +
                "- **Main Category:** Look for the largest, centered, or boldest headers (e.g., 'PLATS', 'ENTRÉES', 'La Boulangerie'). Ignore the restaurant name.\n" +
                "- **Subcategory:** Look for smaller headers underneath a Main Category. NOTE: NOT EVERY CATEGORY HAS SUBCATEGORIES.\n" +
                
                "ITEM EXTRACTION RULES:\n" +
                "1. Each dish name must be separated from its description.\n" +
                "2. **PRICE HANDLING IS CRITICAL:** Extract the price as a NUMBER only (e.g., 11.50). If no price, use 0.\n" +
                "3. **UNIT DETECTION (For Bakeries/Groceries):** If the price is accompanied by a unit (e.g., '5,20€/KG', '4,40 €/100 g', '26,- € / 4 Personnes'), put ONLY the unit part (e.g., 'KG', '100g', '4 Personnes') into the 'priceUnit' field. Do not include the slash '/'. If it's a standard flat price, leave 'priceUnit' as an empty string \"\".\n" +
                "4. **NO FAKE DATA:** If a category has items directly under it without a subcategory header, put them directly in the 'items' array and leave 'subCategories' as an empty array []. DO NOT create a subcategory with a blank name.\n" +

                "JSON SCHEMA (STRICT):\n" +
                "Return ONLY a JSON object. Do not wrap in markdown code blocks. Follow this exact structure:\n" +
                "{\n" +
                "  \"categories\": [\n" +
                "    {\n" +
                "      \"categoryName\": \"Name of Main Category (e.g. La Boulangerie)\",\n" +
                "      \"items\": [\n" + 
                "        { \"name\": \"LE TOUR DE MEULE\", \"description\": \"Levain naturel...\", \"price\": 5.50, \"priceUnit\": \"KG\" },\n" +
                "        { \"name\": \"LE PAIN DE SEIGLE\", \"description\": \"\", \"price\": 2.10, \"priceUnit\": \"\" }\n" +
                "      ],\n" +
                "      \"subCategories\": [\n" +
                "        {\n" +
                "          \"name\": \"Name of Subcategory (ONLY IF IT ACTUALLY EXISTS)\",\n" +
                "          \"items\": [\n" +
                "            { \"name\": \"Subcategory Dish\", \"description\": \"Description\", \"price\": 12.00, \"priceUnit\": \"\" }\n" +
                "          ]\n" +
                "        }\n" +
                "      ]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        // Build Request Body
        Map<String, Object> imageContent = Map.of("type", "image_url", "image_url", Map.of("url", imageUrl));
        Map<String, Object> textContent = Map.of("type", "text", "text", prompt);
        
        Map<String, Object> message = Map.of(
            "role", "user",
            "content", List.of(textContent, imageContent)
        );

        Map<String, Object> requestBody = Map.of(
            "model", "gpt-4o",
            "messages", List.of(message),
            "max_tokens", 4000
        );

        // Send Request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.set("User-Agent", "Mozilla/5.0"); 

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(OPENAI_URL, entity, Map.class);
            
            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
            String jsonContent = (String) ((Map<String, Object>) choices.get(0).get("message")).get("content");

            jsonContent = jsonContent.replace("```json", "").replace("```", "").trim();

            return objectMapper.readValue(jsonContent, MenuImportResult.class);

        } catch (HttpClientErrorException e) {
            System.err.println("OpenAI Error: " + e.getResponseBodyAsString());
            throw new RuntimeException("OpenAI API Error: " + e.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to parse menu with AI: " + e.getMessage());
        }
    }
}