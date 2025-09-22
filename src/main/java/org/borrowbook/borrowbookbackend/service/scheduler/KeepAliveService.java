package org.borrowbook.borrowbookbackend.service.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeepAliveService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${app.base-url}")
    private String baseUrl;

    @Scheduled(fixedRate = 780000)
    public void keepAlive() {
        try {
            restTemplate.getForObject(baseUrl + "/api/v1/auth/get", Void.class);
        } catch (Exception e) {
            log.warn("Keep-alive ping to /get failed: {}", e.getMessage());
        }
    }
}