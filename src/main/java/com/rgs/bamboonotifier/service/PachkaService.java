package com.rgs.bamboonotifier.service;

import com.rgs.bamboonotifier.interfaces.ImessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PachkaService implements ImessageSender {

    private static final Logger logger = LoggerFactory.getLogger(PachkaService.class);

    @Value("${pachka.api.url}")
    private String pachkaApiUrl;

    @Value("${pachka.api.token}")
    private String pachkaApiToken;

    @Value("${pachka.api.entity_id}")
    private String entity_id;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendMessage(String message) {
        HttpEntity request = buildRequest(message);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(pachkaApiUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Сообщение отправлено: {}", response.getBody());
            } else {
                logger.error("Ошибка: {} - {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            logger.error("Ошибка при отправке в пачку: {}", e.getMessage());
            throw e;
        }
    }

    private HttpEntity buildRequest(String content) {
        Map<String, Object> message = new HashMap<>();
        message.put("entity_type", "discussion");
        message.put("entity_id", entity_id);
        message.put("content", content);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(pachkaApiToken);

        return new HttpEntity<>(requestBody, headers);
    }

}
