package com.example.app.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

//import javax.mail.MessagingException;
//import javax.mail.internet.MimeMessage;
import java.io.File;

@Service
public class SendEmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendSimpleEmail(String toEmail,
                                String subject,
                                String body
    ) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("integrativetest1@outlook.com");
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);
        try {
            mailSender.send(message);
            System.out.println("Notification email sent successfully to " + toEmail);
        } catch (Exception e) {
            System.out.println("Failed to send email to " + toEmail + " due to: " + e.getMessage());
        }

    }

    
}