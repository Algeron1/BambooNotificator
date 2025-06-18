
package com.rgs.bamboonotifier.service;

import com.rgs.bamboonotifier.DTO.TelegramResponse;
import com.rgs.bamboonotifier.config.TelegramProperties;
import com.rgs.bamboonotifier.interfaces.IMessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramService implements IMessageSender<TelegramResponse> {

    private static final Logger logger = LoggerFactory.getLogger(TelegramService.class);

    private final RestTemplate restTemplate = new RestTemplate();
    private final TelegramProperties telegramProperties;

    public TelegramService(TelegramProperties telegramProperties) {
        this.telegramProperties = telegramProperties;
    }

    @Override
    public ResponseEntity<TelegramResponse> sendMessage(String message, String messageId) {
        HttpEntity<Map<String, Object>> request = buildRequest(message, messageId);
        try {
            ResponseEntity<TelegramResponse> response = restTemplate.postForEntity(telegramProperties.getUrl(), request, TelegramResponse.class);
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

    private HttpEntity<Map<String, Object>> buildRequest(String content, String messageId) {
        Map<String, Object> params = new HashMap<>();
        params.put("chat_id", telegramProperties.getChatId());
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
