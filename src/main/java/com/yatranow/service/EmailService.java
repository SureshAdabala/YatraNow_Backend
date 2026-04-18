package com.yatranow.service;

import com.yatranow.dto.BookingResponse;
import com.yatranow.entity.User;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final PdfService pdfService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends the booking confirmation email asynchronously without blocking the request thread.
     */
    public void sendBookingConfirmation(User user, String razorpayOrderId, List<BookingResponse> bookings) {
        log.info("Preparing to send confirmation email to {}", user.getEmail());

        try {
            // Generate PDF Ticket
            byte[] pdfBytes = pdfService.generateTicketPdf(bookings, user, razorpayOrderId);

            // Construct HTML Email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, "YatraNow Bookings");
            helper.setTo(user.getEmail());
            helper.setSubject("Ticket Booking Confirmation - Order: " + razorpayOrderId);

            String htmlContent = buildHtmlContent(user, razorpayOrderId, bookings);
            helper.setText(htmlContent, true);

            // Attach PDF
            helper.addAttachment("YatraNow_Tickets_" + razorpayOrderId + ".pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            log.info("Successfully sent booking confirmation email to {}", user.getEmail());

        } catch (Exception e) {
            log.error("Failed to send booking confirmation email to {}: {}", user.getEmail(), e.getMessage(), e);
            // We log the error but DO NOT re-throw, to avoid breaking the payment verification flow
        }
    }

    private String buildHtmlContent(User user, String orderId, List<BookingResponse> bookings) {
        StringBuilder html = new StringBuilder();

        // Safe extraction of the first booking's route details for the email header
        BookingResponse firstBooking = bookings.isEmpty() ? null : bookings.get(0);
        String routeStr = firstBooking != null ? 
            (firstBooking.fromLocation() + " &rarr; " + firstBooking.toLocation()) : "Your Selected Route";

        html.append("<!DOCTYPE html>")
            .append("<html><head>")
            .append("<style>")
            .append("body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f6f9fc; color: #333; margin: 0; padding: 0; }")
            .append(".container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }")
            .append(".header { background-color: #FF7A1A; color: #ffffff; padding: 20px; text-align: center; }")
            .append(".header h1 { margin: 0; font-size: 24px; }")
            .append(".content { padding: 30px; }")
            .append(".greeting { font-size: 18px; margin-bottom: 20px; }")
            .append(".info-box { background: #fdf2ea; border-left: 4px solid #FF7A1A; padding: 15px; margin-bottom: 25px; border-radius: 4px; }")
            .append(".info-box p { margin: 5px 0; }")
            .append(".footer { background-color: #2b2b2b; color: #bbbbbb; padding: 20px; text-align: center; font-size: 12px; }")
            .append(".btn { display: inline-block; padding: 12px 24px; background-color: #FF7A1A; color: #ffffff; text-decoration: none; border-radius: 4px; font-weight: bold; margin-top: 20px; }")
            .append("</style>")
            .append("</head><body>")
            .append("<div class='container'>")
            .append("  <div class='header'>")
            .append("    <h1>Ticket Booking Confirmation</h1>")
            .append("  </div>")
            .append("  <div class='content'>")
            .append("    <p class='greeting'>Dear ").append(user.getName()).append(",</p>")
            .append("    <p>Thank you for choosing YatraNow! Your payment is successful, and your seats have been confirmed.</p>")
            .append("    <div class='info-box'>")
            .append("      <p><strong>Order ID:</strong> ").append(orderId).append("</p>")
            .append("      <p><strong>Route:</strong> ").append(routeStr).append("</p>")
            .append("      <p><strong>Total Tickets:</strong> ").append(bookings.size()).append("</p>")
            .append("      <p><strong>Status:</strong> COMPLETED</p>")
            .append("    </div>")
            .append("    <p>We have attached your official e-ticket(s) to this email. Please download the PDF and present the enclosed QR code(s) when boarding.</p>")
            .append("    <p>If you have any questions, feel free to reach out to our support team.</p>")
            .append("    <p>Have a safe and wonderful journey!</p>")
            .append("  </div>")
            .append("  <div class='footer'>")
            .append("    <p>&copy; 2026 YatraNow. All rights reserved.</p>")
            .append("    <p>Need Help? Contact us at support@yatranow.com</p>")
            .append("  </div>")
            .append("</div>")
            .append("</body></html>");

        return html.toString();
    }
}
