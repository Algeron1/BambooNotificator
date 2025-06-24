package com.rgs.bamboonotifier.Rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.AnnouncementMessageInfo;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.constants.ApplicationConstants;
import com.rgs.bamboonotifier.sender.MessageSender;
import com.rgs.bamboonotifier.service.NotificationSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@RestController()
@RequestMapping("/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final MessageSender messageSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final NotificationSettingsService settingsService;

    public AdminController(MessageSender messageSender,
                           RedisTemplate<String, String> redisTemplate,
                           ObjectMapper objectMapper,
                           NotificationSettingsService settingsService) {
        this.messageSender = messageSender;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.settingsService = settingsService;
    }

    @PostMapping("/announcement")
    public ResponseEntity<String> createAnnouncement(@RequestBody AnnouncementMessage announcement) {
        try {
            announcement.setId(UUID.randomUUID().toString());
            if (announcement.getTo() == null) {
                announcement.setTo(LocalDateTime.now().plusHours(24));
            }
            String key = "announcementMessage:" + announcement.getId();
            Duration ttl = Duration.between(LocalDateTime.now(), announcement.getTo());
            String json = objectMapper.writeValueAsString(announcement);
            redisTemplate.opsForValue().set(key, json, ttl);
            sendAnnouncementMessage(announcement);
            return ResponseEntity.ok("Объявление создано");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementMessageInfo>> getAnnouncements() {
        try {
            Set<String> keys = redisTemplate.keys("announcementMessage:*");

            if (keys == null || keys.isEmpty()) {
                logger.info("Объявлений не найдено");
                return ResponseEntity.ok(Collections.emptyList());
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
            return ResponseEntity.ok(infos);

        } catch (Exception e) {
            logger.error("Ошибка при получении объявлений: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sendText")
    public ResponseEntity<String> sendText(@RequestBody String text) {
        try {
           sendTextMessage(text);
            return ResponseEntity.ok("Сообщение отправлено");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }


    @DeleteMapping("/announcement/{id}")
    public ResponseEntity<String> deleteAnnouncement(@PathVariable String id) {
        String key = "announcementMessage:" + id;
        if (!redisTemplate.hasKey(key)) {
            return ResponseEntity.status(404).body("Объявление не найдено");
        }
        redisTemplate.delete(key);
        return ResponseEntity.ok("Объявление удалено");
    }

    @PostMapping("/changesettings")
    public ResponseEntity<String> updateSettings(@RequestBody Map<String, Boolean> newSettings) {
        newSettings.forEach(settingsService::updateSetting);
        return ResponseEntity.ok("Настройки обновлены");
    }

    @GetMapping("/currentsettings")
    public Map<String, Boolean> getCurrentSettings() {
        Map<String, Boolean> settings = new HashMap<>();
        settings.put("notification.telegram.enabled", settingsService.isTelegramEnabled());
        settings.put("notification.pachka.enabled", settingsService.isPachkaEnabled());
        return settings;
    }


    private void sendAnnouncementMessage(AnnouncementMessage announcementMessage) {
        sendTextMessage(formatAnnouncementMessage(announcementMessage));
    }

    private void sendTextMessage(String text) {
        try {
            messageSender.sendTextMessage(text);
            logger.info("Отправка сообщения через панель администратора успешна");
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения через панель администратора: {}", e.getMessage());
        }
    }

    private String formatAnnouncementMessage(AnnouncementMessage announcementMessage) {
        return String.format(ApplicationConstants.ANNOUNCEMENT_MESSAGE_TEMPLATE,
                getWarningLevel(announcementMessage),
                announcementMessage.getAuthor(),
                announcementMessage.getText()
        );
    }

    private String getWarningLevel(AnnouncementMessage announcementMessage) {
        return switch (announcementMessage.getWarningLevel()) {
            case "CRITICAL" -> ApplicationConstants.WARNING_CRITICAL;
            case "WARNING" -> ApplicationConstants.WARNING_IMPORTANT;
            case "INFO" -> ApplicationConstants.WARNING_INFO;
            default -> throw new IllegalArgumentException("не выбрана важность объявления");
        };
    }
}
