
package com.rgs.bamboonotifier.service;

import com.rgs.bamboonotifier.DTO.TelegramResponse;
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
public class TelegramService implements ImessageSender<TelegramResponse> {

    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);

    @Value("${telegram.api.url}")
    private String telegramApiUrl;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ResponseEntity<TelegramResponse> sendMessage(String message, String messageId) {
        HttpEntity request = buildRequest(message, messageId);
        try {
            ResponseEntity<TelegramResponse> response = restTemplate.postForEntity(telegramApiUrl, request, TelegramResponse.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Сообщение отправлено: {}", response.getBody());
            } else {
                logger.error("Ошибка: {} - {}", response.getStatusCode(), response.getBody());
            }
            return response;
        } catch (Exception e) {
            logger.error("Ошибка при отправке в телеграм: {}", e.getMessage());
            throw e;
        }
    }

    private HttpEntity buildRequest(String content, String messageId) {
        Map<String, String> params = new HashMap<>();
        params.put("chat_id", chatId);
        params.put("text", content);
        params.put("parse_mode", "HTML");
        if (messageId != null) {
            params.put("reply_to_message_id", messageId);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(params, headers);
    }
}
