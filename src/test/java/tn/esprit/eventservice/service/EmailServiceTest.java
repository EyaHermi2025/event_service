package tn.esprit.eventservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private TemplateEngine templateEngine;
    @Mock private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Inject the @Value field that Spring would normally populate
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@test.com");
    }

    @Test
    void testSendEmailWithAttachment_Success() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendEmailWithAttachment("to@test.com", "Subject", "<b>Body</b>",
                new byte[]{1, 2}, "file.pdf");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendEmailWithAttachment_NullAttachment() {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        emailService.sendEmailWithAttachment("to@test.com", "Subject", "Body", null, "file.pdf");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void testSendEmailWithAttachment_Exception() {
        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail error"));
        assertDoesNotThrow(() ->
                emailService.sendEmailWithAttachment("to@test.com", "Sub", "Body", null, "f.pdf"));
    }

    @Test
    void testGenerateHtmlFromTemplate() {
        when(templateEngine.process(eq("template"), any(Context.class))).thenReturn("<html>OK</html>");
        String result = emailService.generateHtmlFromTemplate("template", Map.of("key", "value"));
        assertEquals("<html>OK</html>", result);
    }
}
