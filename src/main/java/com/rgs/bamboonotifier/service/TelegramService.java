
package com.rgs.bamboonotifier.service;

import com.rgs.bamboonotifier.interfaces.ImessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService implements ImessageSender {

    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);

    @Value("${telegram.api.url}")
    private String telegramApiUrl;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public void sendMessage(String message) {
        HttpEntity request = buildRequest(message);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(telegramApiUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Сообщение отправлено: {}", response.getBody());
            } else {
                logger.error("Ошибка: {} - {}", response.getStatusCode(), response.getBody());
            }
        } catch (Exception e) {
            logger.error("Ошибка при отправке в телеграм: {}", e.getMessage());
            throw e;
        }
    }

    private HttpEntity buildRequest(String content) {
        Map<String, String> params = new HashMap<>();
        params.put("chat_id", chatId);
        params.put("text", content);
        params.put("parse_mode", "HTML");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(params, headers);
    }
}
