package com.atm.service;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class EmailService {

    private final Session session;
    private final String username;
    private final String password;

    public EmailService() {
        // Read properties safely
        String host = System.getProperty("mail.smtp.host", System.getProperty("mail.host", "localhost"));
        String port = System.getProperty("mail.smtp.port", System.getProperty("mail.port", "25"));
        this.username = System.getProperty("mail.smtp.user", System.getProperty("mail.username", ""));
        this.password = System.getProperty("mail.smtp.password", System.getProperty("mail.password", ""));

        Properties props = new Properties();
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        this.session = Session.getInstance(props);
    }

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);

        // Send the message
        Transport.send(message);
    }
}
