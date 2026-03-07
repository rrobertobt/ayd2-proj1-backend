package edu.robertob.ayd2_p1_backend.core.services;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.ITemplateEngine;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock private JavaMailSender mailSender;
    @Mock private ITemplateEngine templateEngine;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() throws Exception {
        Field fromField = MailService.class.getDeclaredField("from");
        fromField.setAccessible(true);
        fromField.set(mailService, "noreply@test.com");
    }

    @Test
    void sendHtmlEmail_success_callsSend() {
        Session session = Session.getInstance(new Properties());
        MimeMessage mimeMessage = new MimeMessage(session);

        when(templateEngine.process(anyString(), any())).thenReturn("<html><body>Hello</body></html>");
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> mailService.sendHtmlEmail(
                "user@mail.com",
                "Test Subject",
                "email/test-template",
                Map.of("key", "value")
        ));

        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendHtmlEmail_messagingException_logsErrorWithoutThrowing() {
        Session session = Session.getInstance(new Properties());

        // Anonymous subclass that throws MessagingException on setFrom()
        MimeMessage badMessage = new MimeMessage(session) {
            @Override
            public void setFrom(Address address) throws MessagingException {
                throw new MessagingException("Forced SMTP error for test");
            }
        };

        when(templateEngine.process(anyString(), any())).thenReturn("<html><body>Hello</body></html>");
        when(mailSender.createMimeMessage()).thenReturn(badMessage);

        // MessagingException is caught inside sendHtmlEmail — should NOT propagate
        assertDoesNotThrow(() -> mailService.sendHtmlEmail(
                "user@mail.com",
                "Test Subject",
                "email/test-template",
                Map.of("key", "value")
        ));

        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
