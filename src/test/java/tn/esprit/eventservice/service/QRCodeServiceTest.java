package tn.esprit.eventservice.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class QRCodeServiceTest {

    private final QRCodeService qrCodeService = new QRCodeService();

    @Test
    void testGenerateQRCodeImage() throws Exception {
        byte[] image = qrCodeService.generateQRCodeImage("http://localhost:4200/verify/1", 200, 200);
        assertNotNull(image);
        assertTrue(image.length > 0);
    }

    @Test
    void testGenerateQRCodeImage_DifferentSizes() throws Exception {
        byte[] small = qrCodeService.generateQRCodeImage("test", 100, 100);
        byte[] large = qrCodeService.generateQRCodeImage("test", 400, 400);
        assertNotNull(small);
        assertNotNull(large);
        assertTrue(large.length > small.length);
    }

    @Test
    void testGenerateQRCodeBase64() throws Exception {
        String base64 = qrCodeService.generateQRCodeBase64("http://localhost:4200/verify/1", 200, 200);
        assertNotNull(base64);
        assertFalse(base64.isEmpty());
        // Base64 chars only
        assertTrue(base64.matches("[A-Za-z0-9+/=]+"));
    }

    @Test
    void testGenerateQRCodeImage_LongText() throws Exception {
        String longText = "http://localhost:4200/verify-ticket/12345?seat=A1&event=Workshop+Advanced+Java+Spring+Boot";
        byte[] image = qrCodeService.generateQRCodeImage(longText, 300, 300);
        assertNotNull(image);
        assertTrue(image.length > 0);
    }
}
