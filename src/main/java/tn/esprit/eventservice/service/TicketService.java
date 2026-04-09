package tn.esprit.eventservice.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventRegistration;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class TicketService {

    private final QRCodeService qrCodeService;
    private final EmailService emailService;

    public TicketService(QRCodeService qrCodeService, EmailService emailService) {
        this.qrCodeService = qrCodeService;
        this.emailService = emailService;
    }

    public void generateAndSendTicket(Event event, EventRegistration registration) {
        try {
            // 1. Generate QR Code containing event details
            String payment = registration.getPaymentMethod() != null ? registration.getPaymentMethod().name() : "N/A";
            String level = registration.getLevel() != null ? registration.getLevel() : "N/A";
            String qrData = "Événement : " + event.getTitle() + "\n" +
                    "Participant : " + registration.getUserName() + "\n" +
                    "Ticket N° : REG-" + registration.getId() + "\n" +
                    "Statut : Confirmé (" + payment + ")\n" +
                    "Niveau : " + level;
            byte[] qrCodeImage = qrCodeService.generateQRCodeImage(qrData, 200, 200);

            // 2. Generate PDF Ticket
            byte[] pdfTicket = createPdfTicket(event, registration, qrCodeImage);

            // 3. Prepare Email Content
            Map<String, Object> variables = new HashMap<>();
            variables.put("userName", registration.getUserName());
            variables.put("eventTitle", event.getTitle());

            // Note: We'll create this simple template next
            String emailBody = "Bonjour " + registration.getUserName() + ",<br><br>" +
                    "Votre inscription à l'événement <b>" + event.getTitle() + "</b> a été confirmée.<br>" +
                    "Veuillez trouver votre billet en pièce jointe.<br><br>" +
                    "Cordialement,<br>L'équipe SchoolPlatform";

            // 4. Send Email
            emailService.sendEmailWithAttachment(
                    registration.getUserEmail(),
                    "Confirmation d'inscription : " + event.getTitle(),
                    emailBody,
                    pdfTicket,
                    "Ticket_" + event.getTitle().replaceAll("\\s+", "_") + ".pdf");

        } catch (Exception e) {
            System.err.println("Error generating ticket: " + e.getMessage());
        }
    }

    private byte[] createPdfTicket(Event event, EventRegistration registration, byte[] qrCodeImage) throws Exception {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);

        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, colorToPdfColor("#2D5757"));
        Paragraph title = new Paragraph("OFFICIAL EVENT TICKET", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Event Details
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("EVENT: " + event.getTitle().toUpperCase(), labelFont));
        document.add(new Paragraph("OWNER: " + registration.getUserName(), valueFont));
        document.add(new Paragraph("EMAIL: " + registration.getUserEmail(), valueFont));
        document.add(new Paragraph("DATE: " + (event.getStartDate() != null ? event.getStartDate().toString() : "TBD"),
                valueFont));
        document.add(new Paragraph("LOCATION: Consult SchoolPlatform", valueFont));

        document.add(new Paragraph("\n"));

        // QR Code
        Image qrImage = Image.getInstance(qrCodeImage);
        qrImage.setAlignment(Element.ALIGN_CENTER);
        qrImage.scaleToFit(150, 150);
        document.add(qrImage);

        // Footer
        Paragraph footer = new Paragraph("\nScannez ce code à l'entrée de l'événement.", valueFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
        return out.toByteArray();
    }

    private java.awt.Color colorToPdfColor(String hex) {
        return java.awt.Color.decode(hex);
    }
}
