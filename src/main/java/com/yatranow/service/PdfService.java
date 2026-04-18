package com.yatranow.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.yatranow.dto.BookingResponse;
import com.yatranow.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Slf4j
@Service
public class PdfService {

    public byte[] generateTicketPdf(List<BookingResponse> bookings, User user, String razorpayOrderId) {
        log.info("Generating PDF ticket for order: {}", razorpayOrderId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 30, 30, 50, 50);
            PdfWriter.getInstance(document, out);
            document.open();

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
            Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("YatraNow - E-Ticket", headerFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            Paragraph orderInfo = new Paragraph("Booking Reference (Order ID): " + razorpayOrderId, subHeaderFont);
            orderInfo.setAlignment(Element.ALIGN_CENTER);
            orderInfo.setSpacingAfter(10);
            document.add(orderInfo);

            Paragraph userInfo = new Paragraph("Booked By: " + user.getName() + " (" + user.getEmail() + ")", normalFont);
            userInfo.setAlignment(Element.ALIGN_CENTER);
            userInfo.setSpacingAfter(30);
            document.add(userInfo);

            for (BookingResponse booking : bookings) {
                // Outer table for the ticket border
                PdfPTable ticketTable = new PdfPTable(1);
                ticketTable.setWidthPercentage(100);
                ticketTable.setSpacingAfter(30);

                PdfPCell outerCell = new PdfPCell();
                outerCell.setPadding(15);
                outerCell.setBorderWidth(2);
                outerCell.setBorderColor(new Color(255, 122, 26)); // YatraNow Orange

                // Inner layout with 2 columns: Details | QR Code
                PdfPTable innerTable = new PdfPTable(new float[]{3f, 1f});
                innerTable.setWidthPercentage(100);

                // Details Cell
                PdfPCell detailsCell = new PdfPCell();
                detailsCell.setBorder(Rectangle.NO_BORDER);

                detailsCell.addElement(new Paragraph("Ticket ID: " + booking.bookingId(), boldFont));
                detailsCell.addElement(new Paragraph("Passenger: " + booking.passengerName() +
                        " | Age: " + booking.passengerAge() + " | Gender: " + booking.passengerGender(), normalFont));
                detailsCell.addElement(new Paragraph("Seat: " + booking.seatNumber(), boldFont));
                detailsCell.addElement(new Paragraph("Route: " + booking.fromLocation() + " to " + booking.toLocation(), normalFont));
                detailsCell.addElement(new Paragraph("Date & Time: " + booking.departureTime() + " - " + booking.arrivalTime(), normalFont));
                detailsCell.addElement(new Paragraph("Vehicle: " + booking.vehicleName() + " (" + booking.vehicleNumber() + ")", normalFont));
                detailsCell.addElement(new Paragraph("Status: " + booking.status(), normalFont));

                innerTable.addCell(detailsCell);

                // QR Code Cell
                PdfPCell qrCell = new PdfPCell();
                qrCell.setBorder(Rectangle.NO_BORDER);
                qrCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                qrCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

                try {
                    // Generate QR code for the specific booking ID
                    byte[] qrImageBytes = generateQrCodeImage(String.valueOf(booking.bookingId()));
                    Image qrImage = Image.getInstance(qrImageBytes);
                    qrImage.scaleAbsolute(100f, 100f);
                    qrImage.setAlignment(Element.ALIGN_RIGHT);
                    qrCell.addElement(qrImage);
                } catch (Exception e) {
                    log.error("Failed to generate QR code for ticket {}: {}", booking.bookingId(), e.getMessage());
                    qrCell.addElement(new Paragraph("QR Not Available"));
                }

                innerTable.addCell(qrCell);

                outerCell.addElement(innerTable);
                ticketTable.addCell(outerCell);

                document.add(ticketTable);
            }

            document.close();
            return out.toByteArray();

        } catch (Exception e) {
            log.error("Error generating PDF ticket", e);
            throw new RuntimeException("Could not generate PDF ticket");
        }
    }

    private byte[] generateQrCodeImage(String text) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
}
