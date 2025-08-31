package com.dass.foodordering.food_ordering_backend.service;

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
        String subject = "New Online Order Received! #" + order.getId();
        String body = String.format(
            "<h1>New Order Alert!</h1>" +
            "<p>You have received a new order with a total of $%.2f.</p>" +
            "<p>Please log in to your dashboard to view the details and confirm the order.</p>",
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
        String subject = "Your order from " + order.getRestaurant().getName() + " is confirmed!";
        String body = String.format(
            "<h1>Your Order is Confirmed!</h1>" +
            "<p>Hello,</p>" +
            "<p>Your order #%d has been confirmed by the restaurant and is now being prepared.</p>" +
            "<p>Total: $%.2f</p>" +
            "<p>Thank you for your order!</p>",
            order.getId(),
            order.getTotalPrice()
        );
        sendEmail(customerEmail, subject, body);
    }

    public void sendReservationConfirmedNotification(Reservation reservation) {
        String customerEmail = reservation.getCustomerEmail();
        String subject = "Your Reservation is Confirmed!";
        String body = String.format(
            "<h1>Reservation Confirmed!</h1>" +
            "<p>Hello %s,</p>" +
            "<p>Your table reservation for %d people at %s on %s has been confirmed.</p>" +
            "<p>We look forward to seeing you!</p>",
            reservation.getCustomerName(),
            reservation.getPartySize(),
            reservation.getRestaurant().getName(),
            reservation.getReservationTime().toString() // You can format this date better
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
}