package tn.esprit.eventservice.service;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import tn.esprit.eventservice.entity.Event;
import tn.esprit.eventservice.entity.EventRegistration;
import tn.esprit.eventservice.entity.EventType;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock private QRCodeService qrCodeService;
    @Mock private EmailService emailService;

    @InjectMocks
    private TicketService ticketService;

    @Test
    void testGenerateAndSendTicket_Success() throws Exception {
        Event event = Event.builder().id(1L).title("Workshop A")
                .startDate(LocalDateTime.now().plusDays(1)).type(EventType.WORKSHOP).build();
        EventRegistration reg = EventRegistration.builder().id(10L)
                .userName("Eya").userEmail("eya@test.com").seatNumber("A1").build();

        when(qrCodeService.generateQRCodeImage(anyString(), eq(200), eq(200)))
                .thenReturn(new byte[]{1, 2, 3});

        ticketService.generateAndSendTicket(event, reg);

        verify(qrCodeService).generateQRCodeImage(contains("10"), eq(200), eq(200));
        verify(emailService).sendEmailWithAttachment(
                eq("eya@test.com"), contains("Workshop A"), anyString(), any(byte[].class), contains("Workshop_A"));
    }

    @Test
    void testGenerateAndSendTicket_NullSeat() throws Exception {
        Event event = Event.builder().id(1L).title("Event X")
                .startDate(LocalDateTime.now()).type(EventType.MEETING).build();
        EventRegistration reg = EventRegistration.builder().id(5L)
                .userName("User").userEmail("user@test.com").seatNumber(null).build();

        when(qrCodeService.generateQRCodeImage(anyString(), eq(200), eq(200)))
                .thenReturn(new byte[]{1});

        ticketService.generateAndSendTicket(event, reg);
        verify(emailService).sendEmailWithAttachment(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void testGenerateAndSendTicket_NullStartDate() throws Exception {
        Event event = Event.builder().id(1L).title("No Date")
                .startDate(null).type(EventType.COMPETITION).build();
        EventRegistration reg = EventRegistration.builder().id(7L)
                .userName("Test").userEmail("t@t.com").seatNumber("B2").build();

        when(qrCodeService.generateQRCodeImage(anyString(), eq(200), eq(200)))
                .thenReturn(new byte[]{1});

        ticketService.generateAndSendTicket(event, reg);
        verify(emailService).sendEmailWithAttachment(anyString(), anyString(), anyString(), any(), anyString());
    }

    @Test
    void testGenerateAndSendTicket_QRCodeException() throws Exception {
        Event event = Event.builder().id(1L).title("Fail QR").build();
        EventRegistration reg = EventRegistration.builder().id(1L)
                .userName("U").userEmail("u@t.com").build();

        when(qrCodeService.generateQRCodeImage(anyString(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("QR Error"));

        ticketService.generateAndSendTicket(event, reg);
        verify(emailService, never()).sendEmailWithAttachment(any(), any(), any(), any(), any());
    }

    @Test
    void testGenerateAndSendTicket_TitleWithSpaces() throws Exception {
        Event event = Event.builder().id(1L).title("My Big Event Here")
                .startDate(LocalDateTime.now()).build();
        EventRegistration reg = EventRegistration.builder().id(1L)
                .userName("U").userEmail("u@t.com").seatNumber("C3").build();

        when(qrCodeService.generateQRCodeImage(anyString(), eq(200), eq(200)))
                .thenReturn(new byte[]{1});

        ticketService.generateAndSendTicket(event, reg);
        verify(emailService).sendEmailWithAttachment(anyString(), anyString(), anyString(), any(),
                eq("Ticket_My_Big_Event_Here.pdf"));
    }
}
