package com.rgs.bamboonotifier.service;

import com.rgs.bamboonotifier.DTO.PachkaResponse;
import com.rgs.bamboonotifier.config.PachkaProperties;
import com.rgs.bamboonotifier.interfaces.IMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class PachkaService implements IMessageSender<PachkaResponse> {

    private static final Logger logger = LoggerFactory.getLogger(PachkaService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final PachkaProperties pachkaProperties;

    public PachkaService(PachkaProperties pachkaProperties) {
        this.pachkaProperties = pachkaProperties;
    }

    @Override
    public ResponseEntity<PachkaResponse> sendMessage(String message, String messageId) {
        HttpEntity<Map<String, Object>> request = buildRequest(message, messageId);
        try {
            ResponseEntity<PachkaResponse> response = restTemplate.postForEntity(pachkaProperties.getUrl(), request, PachkaResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Сообщение отправлено: {}", response.getBody());
            } else {
                logger.error("Ошибка: {} - {}", response.getStatusCode(), response.getBody());
            }
            return response;
        } catch (Exception e) {
            logger.error("Ошибка при отправке в пачку: {}", e.getMessage());
            throw e;
        }
    }

    private HttpEntity<Map<String, Object>> buildRequest(String content, String messageId) {
        Map<String, Object> message = new HashMap<>();
        message.put("entity_type", "discussion");
        message.put("entity_id", pachkaProperties.getEntity_id());
        message.put("content", content);

        if (messageId != null && !messageId.isEmpty()) {
            message.put("parent_message_id", Integer.parseInt(messageId));
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("message", message);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(pachkaProperties.getToken());

        return new HttpEntity<>(requestBody, headers);
    }

}
