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
        String prompt = "You are a menu extraction API. Your job is to turn this menu image into structured JSON data." +
                
                "HIERARCHY DETECTION:" +
                "- **Main Category:** Look for the largest, centered, or boldest headers (e.g., 'PLATS', 'ENTRÉES', 'Biryani'). Ignore the restaurant name 'AU PUNJAB'." +
                "- **Subcategory:** Look for smaller, underlined, or bold headers underneath a Main Category (e.g., 'Plats à base de poulet', 'Plats à base d'agneau', 'Plats végétarien')." +
                
                "ITEM EXTRACTION RULES:" +
                "1. Each dish name must be separated from its description." +
                "2. **PRICE HANDLING IS CRITICAL:** Extract the price as a NUMBER only. Convert '11,50€' to 11.50. If multiple prices exist, take the first one. If no price, use 0." +
                "3. Do NOT include the price in the item name or description." +

                "JSON SCHEMA (STRICT):" +
                "Return ONLY a JSON object. Do not wrap in markdown code blocks. Use this exact structure:" +
                "{" +
                "  \"categories\": [" +
                "    {" +
                "      \"categoryName\": \"Name of Main Category (e.g. PLATS)\"," +
                "      \"items\": []," + // Use this list ONLY if items have no subcategory
                "      \"subCategories\": [" +
                "        {" +
                "          \"name\": \"Name of Subcategory (e.g. Plats à base de poulet)\"," +
                "          \"items\": [" +
                "            {" +
                "              \"name\": \"Dish Name (e.g. POULET KORMA)\"," +
                "              \"description\": \"Dish Description (e.g. Poulet au curry...)\"," +
                "              \"price\": 11.50" +
                "            }" +
                "          ]" +
                "        }" +
                "      ]" +
                "    }" +
                "  ]" +
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