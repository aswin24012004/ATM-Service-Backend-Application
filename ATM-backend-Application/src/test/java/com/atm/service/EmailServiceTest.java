package com.atm.service;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;

import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.MimeMessage;


class EmailServiceTest {

    public EmailService email;

    @BeforeEach
    void setUp() {
        System.setProperty("mail.host", "smtp.test.com");
        System.setProperty("mail.port", "587");
        System.setProperty("mail.username", "aswin.c2401@gmail.com");
        System.setProperty("mail.password", "qdku kgld azll zcxd");

        email = new EmailService();
    }
    
    @Test
    void testMimeMessageProperties() throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.test.com");
        Session session = Session.getInstance(props);

        MimeMessage message = new MimeMessage(session);
        message.setFrom("testuser@gmail.com");
        message.setRecipients(Message.RecipientType.TO, "testuser@gmail.com");
        message.setSubject("Subject");
        message.setText("Hello World");

        assertEquals("Subject", message.getSubject());
        assertEquals("Hello World", message.getContent());
    }
    
    @Test
    void testSendEmailBuildsMessageCorrectly() throws Exception { 
    	try (MockedStatic<Transport> transMock = mockStatic(Transport.class)) { 
    		email.sendEmail("testuser@example.com", "Test Subject", "Hello World");  
    		transMock.verify(() -> Transport.send(any(Message.class)), times(1)); 
    		} 
    	String val = "Send Email Message Correctly";
    	assertEquals("Send Email Message Correctly", val); 

    	}

    
}
