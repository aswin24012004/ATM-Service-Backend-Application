package service;

import com.atm.service.EmailService;
import com.atm.util.ConfigUtil;
import jakarta.mail.Message;
import jakarta.mail.Transport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        try (MockedStatic<ConfigUtil> configMock = mockStatic(ConfigUtil.class)) {
            configMock.when(() -> ConfigUtil.get("mail.host")).thenReturn("smtp.test.com");
            configMock.when(() -> ConfigUtil.get("mail.port")).thenReturn("587");
            configMock.when(() -> ConfigUtil.get("mail.username")).thenReturn("testuser@example.com");
            configMock.when(() -> ConfigUtil.get("mail.password")).thenReturn("testpass");

            emailService = new EmailService();
        }
    }

    @Test
    void testSendEmailBuildsMessageCorrectly() throws Exception {
        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            emailService.sendEmail("recipient@example.com", "Test Subject", "Hello World");

            // Verify that Transport.send was called once with a Message
            transportMock.verify(() -> Transport.send(any(Message.class)), times(1));
        }
    }

    @Test
    void testSendEmailSetsCorrectFields() throws Exception {
        try (MockedStatic<Transport> transportMock = mockStatic(Transport.class)) {
            emailService.sendEmail("recipient@example.com", "Subject Line", "Email Body");

            transportMock.verify(() -> Transport.send(argThat(message -> {
                try {
                    assertEquals("Subject Line", message.getSubject());
                    assertEquals("Email Body", message.getContent());
                    assertEquals("testuser@example.com", ((jakarta.mail.internet.InternetAddress) message.getFrom()[0]).getAddress());
                    return true;
                } catch (Exception e) {
                    return false;
                }
            })), times(1));
        }
    }
}
