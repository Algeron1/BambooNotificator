package com.rgs.bamboonotifier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.AnnouncementMessageInfo;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.constants.ApplicationConstants;
import com.rgs.bamboonotifier.sender.MessageSender;
import io.lettuce.core.RedisException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AdminControllerService {

    private static final Logger logger = LoggerFactory.getLogger(AdminControllerService.class);

    private final MessageSender messageSender;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, String> redisTemplate;
    private final NotificationSettingsService settingsService;

    public AdminControllerService(MessageSender messageSender,
                                  ObjectMapper objectMapper,
                                  RedisTemplate<String, String> redisTemplate,
                                  NotificationSettingsService settingsService) {
        this.messageSender = messageSender;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.settingsService = settingsService;
    }


    public void sendAnnouncementMessage(AnnouncementMessage announcementMessage) {
        sendTextMessage(formatAnnouncementMessage(announcementMessage));
    }

    public void sendTextMessage(String text) {
        try {
            messageSender.sendTextMessage(text);
            logger.info("Отправка сообщения через панель администратора успешна");
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения через панель администратора: {}", e.getMessage());
        }
    }

    public String formatAnnouncementMessage(AnnouncementMessage announcementMessage) {
        return String.format(ApplicationConstants.ANNOUNCEMENT_MESSAGE_TEMPLATE,
                getWarningLevel(announcementMessage),
                announcementMessage.getAuthor(),
                announcementMessage.getText()
        );
    }

    public String getWarningLevel(AnnouncementMessage announcementMessage) {
        return switch (announcementMessage.getWarningLevel()) {
            case "CRITICAL" -> ApplicationConstants.WARNING_CRITICAL;
            case "WARNING" -> ApplicationConstants.WARNING_IMPORTANT;
            case "INFO" -> ApplicationConstants.WARNING_INFO;
            default -> throw new IllegalArgumentException("не выбрана важность объявления");
        };
    }

    public void createAnnouncement(AnnouncementMessage announcement) throws Exception {
        announcement.setId(UUID.randomUUID().toString());
        if (announcement.getTo() == null) {
            announcement.setTo(LocalDateTime.now().plusHours(24));
        }
        String key = "announcementMessage:" + announcement.getId();
        Duration ttl = Duration.between(LocalDateTime.now(), announcement.getTo());
        String json = objectMapper.writeValueAsString(announcement);
        redisTemplate.opsForValue().set(key, json, ttl);
    }

    public List<AnnouncementMessageInfo> getAllAnnouncement() throws Exception {
        try {
            Set<String> keys = redisTemplate.keys("announcementMessage:*");

            if (keys == null || keys.isEmpty()) {
                logger.info("Объявлений не найдено");
                return Collections.emptyList();
            }

            List<AnnouncementMessageInfo> infos = keys.stream()
                    .map(key -> {
                        String json = redisTemplate.opsForValue().get(key);
                        if (json != null) {
                            try {
                                AnnouncementMessage announcement = objectMapper.readValue(json, AnnouncementMessage.class);
                                AnnouncementMessageInfo info = new AnnouncementMessageInfo();
                                info.setId(announcement.getId());
                                info.setFrom(announcement.getFrom());
                                info.setTo(announcement.getTo());
                                info.setText(announcement.getText());
                                info.setAuthor(announcement.getAuthor());
                                info.setWarningLevel(announcement.getWarningLevel());
                                return info;
                            } catch (Exception e) {
                                logger.error("Ошибка при десериализации объявления: {}", e.getMessage());
                                return null;
                            }
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            logger.info("Найдено {} объявлений", infos.size());
            return infos;

        } catch (Exception e) {
            logger.error("Ошибка при получении объявлений: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void deleteAnnouncement(String id) throws Exception {
        String key = "announcementMessage:" + id;
        if (!redisTemplate.hasKey(key)) {
            throw new RedisException("Объявление не найдено");
        }
        redisTemplate.delete(key);
    }

    public void updateSettings(Map<String, Boolean> newSettings) throws Exception {
        try {
            newSettings.forEach(settingsService::updateSetting);
        } catch (Exception e) {
            logger.error("Ошибка при обновлении настроек: " + e.getMessage());
            throw e;
        }

    }

    public Map<String, Boolean> loadSettings() throws Exception {
        Map<String, Boolean> settings = new HashMap<>();
        try {
            settings.put("notification.telegram.enabled", settingsService.isTelegramEnabled());
            settings.put("notification.pachka.enabled", settingsService.isPachkaEnabled());
        } catch (Exception e) {
            logger.error("Ошибка при загрузке настроек: " + e.getMessage());
            throw e;
        }
        return settings;
    }
}
