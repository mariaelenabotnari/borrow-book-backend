package org.borrowbook.borrowbookbackend.service.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ResendEmailService implements EmailService {

    private static final String URL = "https://api.resend.com/emails";

    private final RestTemplate restTemplate = new RestTemplate();
    private final TemplateEngine templateEngine;

    @Value("${resend.api.key}")
    private String apiKey;

    @Value("${resend.from}")
    private String from;

    @Override
    public void sendVerificationCode(String recipientEmail, String verificationCode) {

        Context context = new Context();
        context.setVariable("code", verificationCode);
        context.setVariable("email", recipientEmail);
        String htmlContent = templateEngine.process("code_verification", context);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
                "from", from,
                "to", List.of(recipientEmail),
                "subject", verificationCode + " - Your Verification Code",
                "html", htmlContent
        );

        restTemplate.postForEntity(URL, new HttpEntity<>(body, headers), String.class);
    }
}
