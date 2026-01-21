package org.borrowbook.borrowbookbackend.service.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Primary
@RequiredArgsConstructor
public class APIEmailService implements EmailService{

    private final RestTemplate restTemplate = new RestTemplate();
    private final TemplateEngine templateEngine;

    @Value("${mailgun.api.key}")
    private String apiKey;

    @Value("${mailgun.domain}")
    private String domain;

    @Override
    public void sendVerificationCode(String recipientEmail, String verificationCode) {

        Context context = new Context();
        context.setVariable("code", verificationCode);
        context.setVariable("email", recipientEmail);
        String htmlContent = templateEngine.process("code_verification", context);
        String url = "https://api.eu.mailgun.net/v3/" + domain + "/messages";

        String auth = "api:" + apiKey;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("from", "BorrowBook <noreply@" + domain + ">");
        map.add("to", recipientEmail);
        map.add("subject", verificationCode + " - Your Verification Code");
        map.add("html", htmlContent);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        restTemplate.postForEntity(url, request, String.class);

    }
}
