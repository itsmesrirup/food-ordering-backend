package com.dass.foodordering.food_ordering_backend.service;

import com.dass.foodordering.food_ordering_backend.dto.request.ContactFormRequest;
import com.dass.foodordering.food_ordering_backend.model.Order;
import com.dass.foodordering.food_ordering_backend.model.Reservation;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sender.email.address}")
    private String fromEmailAddress;

    public void sendNewOrderNotification(Order order) {
        //String restaurantEmail = "email-of-the-restaurant-owner@example.com"; // We will improve this later
        String restaurantEmail = order.getRestaurant().getEmail();
        String subject = "Nouvelle Commande / New Order #" + order.getRestaurantOrderSequence();
        String body = String.format(
            "<div style='font-family: Arial, sans-serif;'>" +
                "<h1>Nouvelle Commande Reçue !</h1>" +
                "<p>Vous avez reçu une nouvelle commande d'un montant de <strong>%.2f €</strong>.</p>" +
                "<p>Veuillez vous connecter à votre tableau de bord pour voir les détails et confirmer la commande.</p>" +
                "<hr>" +
                "<p style='color: #666; font-size: 12px;'>You have received a new order. Please log in to your dashboard to view details.</p>" +
            "</div>",
            order.getTotalPrice()
        );
        if (restaurantEmail == null || restaurantEmail.isEmpty()) {
            System.err.println("Cannot send order notification: Restaurant ID " + order.getRestaurant().getId() + " has no email configured.");
            return;
        }
        sendEmail(restaurantEmail, subject, body);
    }

    public void sendOrderConfirmedNotification(Order order) {
        // We get the customer's email from the order itself
        String customerEmail = order.getCustomer().getEmail();
        String subject = "Votre commande chez " + order.getRestaurant().getName() + " est confirmée !";
        
        String body = String.format(
            "<div style='font-family: Arial, sans-serif;'>" +
                "<h1>Commande Confirmée / Order Confirmed</h1>" +
                "<p>Bonjour,</p>" +
                "<p>Votre commande <strong>#%d</strong> a été confirmée par le restaurant et est en cours de préparation.</p>" +
                "<p><strong>Total : %.2f €</strong></p>" +
                "<p>Merci pour votre commande !</p>" +
                "<hr>" +
                "<p style='color: #666; font-size: 12px;'>Your order has been confirmed by the restaurant and is now being prepared.</p>" +
            "</div>",
            order.getRestaurantOrderSequence(), // Use Friendly Number
            order.getTotalPrice()
        );
        sendEmail(customerEmail, subject, body);
    }

    public void sendReservationConfirmedNotification(Reservation reservation) {
        String customerEmail = reservation.getCustomerEmail();
        String subject = "Votre réservation est confirmée !";
        
        String body = String.format(
            "<div style='font-family: Arial, sans-serif;'>" +
                "<h1>Réservation Confirmée / Reservation Confirmed</h1>" +
                "<p>Bonjour %s,</p>" +
                "<p>Votre réservation pour <strong>%d personnes</strong> chez <strong>%s</strong> le <strong>%s</strong> a été confirmée.</p>" +
                "<p>Nous avons hâte de vous accueillir !</p>" +
                "<hr>" +
                "<p style='color: #666; font-size: 12px;'>Your table reservation has been confirmed. We look forward to seeing you!</p>" +
            "</div>",
            reservation.getCustomerName(),
            reservation.getPartySize(),
            reservation.getRestaurant().getName(),
            reservation.getReservationTime().toString().replace("T", " à ") // Simple formatting for date/time
        );
        sendEmail(customerEmail, subject, body);
    }

    private void sendEmail(String toEmail, String subject, String body) {
        Email from = new Email(fromEmailAddress);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", body);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            System.out.println("Email sent! Status code: " + response.getStatusCode());
        } catch (IOException ex) {
            System.err.println("Error sending email: " + ex.getMessage());
            // In a real app, you'd have more robust error logging here
        }
    }

    // --- 5. Internal Notification (Contact Form) ---
    // This goes to YOU, so English is fine, but let's make it standard.
    public void sendContactFormNotification(ContactFormRequest request) {
        String subject = "New Lead from Tablo Landing Page: " + request.getName();
        
        String body = String.format(
            "<h1>New Contact Request</h1>" +
            "<p><strong>Name:</strong> %s</p>" +
            "<p><strong>Email:</strong> %s</p>" +
            "<p><strong>Restaurant:</strong> %s</p>" +
            "<p><strong>Message:</strong></p>" +
            "<blockquote>%s</blockquote>",
            request.getName(),
            request.getEmail(),
            request.getRestaurantName(),
            request.getMessage()
        );

        sendEmail(fromEmailAddress, subject, body); 
    }

    public void sendOrderCancelledNotification(Order order) {
        String customerEmail = order.getCustomer().getEmail();
        String subject = "Mise à jour de votre commande chez " + order.getRestaurant().getName();
        
        String refundText = (order.getPaymentIntentId() != null) 
            ? "<p><strong>Un remboursement a été initié.</strong> Les fonds apparaîtront sur votre compte sous 5 à 10 jours ouvrables.</p>"
            : "";

        String body = String.format(
        "<div style='font-family: Arial, sans-serif;'>" +
            "<h1>Commande Annulée / Order Cancelled</h1>" +
            "<p>Bonjour,</p>" +
            "<p>Nous avons le regret de vous informer que votre commande <strong>#%d</strong> a été annulée par le restaurant.</p>" +
            "%s" + 
            "<p>Pour toute question, veuillez contacter le restaurant au %s.</p>" +
            "<hr>" +
            "<p style='color: #666; font-size: 12px;'>We regret to inform you that your order has been cancelled. If paid online, a refund has been initiated.</p>" +
        "</div>",
        order.getRestaurantOrderSequence(),
        refundText,
        order.getRestaurant().getPhoneNumber() != null ? order.getRestaurant().getPhoneNumber() : "le restaurant"
        );
        
        sendEmail(customerEmail, subject, body);
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        String subject = "Réinitialisation de votre mot de passe - Tablo";
        
        String body = String.format(
            "<div style='font-family: Arial, sans-serif;'>" +
                "<h1>Réinitialisez votre mot de passe</h1>" +
                "<p>Nous avons reçu une demande de réinitialisation de votre mot de passe.</p>" +
                "<p>Cliquez sur le lien ci-dessous pour définir un nouveau mot de passe :</p>" +
                "<p><a href='%s' style='background-color: #1976d2; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block;'>Réinitialiser le mot de passe</a></p>" +
                "<p>Ou copiez ce lien : <br> %s</p>" +
                "<p>Ce lien expirera dans 1 heure.</p>" +
                "<hr>" +
                "<p style='color: #666; font-size: 12px;'>Si vous n'avez pas demandé cela, vous pouvez ignorer cet email.</p>" +
            "</div>",
            resetLink, resetLink
        );
        
        sendEmail(toEmail, subject, body);
    }
}