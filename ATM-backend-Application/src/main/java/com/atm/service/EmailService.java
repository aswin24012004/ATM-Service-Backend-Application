package com.atm.service;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import com.atm.util.ConfigUtil;

public class EmailService {
    private final Session session;
    private final String from;

    public EmailService() {
        String host = ConfigUtil.get("mail.host");
        String port = ConfigUtil.get("mail.port");
        String username = ConfigUtil.get("mail.username");
        String password = ConfigUtil.get("mail.password");

        this.from = username;

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        this.session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }

    public void sendEmail(String to, String subject, String body) throws MessagingException {
        
    	Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        Transport.send(message);
    }
}
