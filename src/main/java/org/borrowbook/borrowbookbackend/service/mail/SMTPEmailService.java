package org.borrowbook.borrowbookbackend.service.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.borrowbook.borrowbookbackend.exception.EmailServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;

@RequiredArgsConstructor
public class SMTPEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    @Value("${spring.mail.username}")
    private String senderEmail;

    @Override
    public void sendVerificationCode(String recipientEmail, String verificationCode) {
        try {
            Context context = new Context();
            context.setVariable("code", verificationCode);
            context.setVariable("email", recipientEmail);
            String htmlContent = templateEngine.process("code_verification", context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(new InternetAddress(senderEmail, "BorrowBook"));
            helper.setTo(recipientEmail);
            helper.setSubject(verificationCode + " - Your Verification Code");
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new EmailServiceException("Failed to send verification email", e);
        }
    }
}
