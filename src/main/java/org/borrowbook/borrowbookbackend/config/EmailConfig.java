package org.borrowbook.borrowbookbackend.config;

import org.borrowbook.borrowbookbackend.service.mail.APIEmailService;
import org.borrowbook.borrowbookbackend.service.mail.EmailService;
import org.borrowbook.borrowbookbackend.service.mail.SMTPEmailService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;

@Configuration
public class EmailConfig {

    @Bean
    @ConditionalOnProperty(name = "app.mail.provider", havingValue = "smtp", matchIfMissing = true)
    public EmailService smtpEmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        return new SMTPEmailService(mailSender, templateEngine);
    }

    @Bean
    @ConditionalOnProperty(name = "app.mail.provider", havingValue = "mailgun")
    public EmailService mailgunEmailService(TemplateEngine templateEngine) {
        return new APIEmailService(templateEngine);
    }
}