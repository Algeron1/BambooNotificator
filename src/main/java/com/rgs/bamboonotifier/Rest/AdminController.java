package com.rgs.bamboonotifier.Rest;

import com.rgs.bamboonotifier.DTO.AnnouncementMessageInfo;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.service.AdminControllerService;
import io.lettuce.core.RedisException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController()
@RequestMapping("/admin")
public class AdminController {

    private final AdminControllerService adminControllerService;

    public AdminController(AdminControllerService adminControllerService) {
        this.adminControllerService = adminControllerService;
    }

    @PostMapping("/announcement")
    public ResponseEntity<String> createAnnouncement(@RequestBody AnnouncementMessage announcement) {
        try {
            adminControllerService.createAnnouncement(announcement);
            adminControllerService.sendAnnouncementMessage(announcement);
            return ResponseEntity.ok("Объявление создано");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }

    @GetMapping("/announcements")
    public ResponseEntity<List<AnnouncementMessageInfo>> getAnnouncements() {
        try {
            return ResponseEntity.ok(adminControllerService.getAllAnnouncement());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/sendText")
    public ResponseEntity<String> sendText(@RequestBody String text) {
        try {
            adminControllerService.sendTextMessage(text);
            return ResponseEntity.ok("Сообщение отправлено");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
    }


    @DeleteMapping("/announcement/{id}")
    public ResponseEntity<String> deleteAnnouncement(@PathVariable String id) {
        try {
            adminControllerService.deleteAnnouncement(id);
        } catch (RedisException redisEx) {
            return ResponseEntity.status(404).body(redisEx.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка при удалении: " + e.getMessage());
        }
        return ResponseEntity.ok("Объявление удалено");
    }

    @PostMapping("/changesettings")
    public ResponseEntity<String> updateSettings(@RequestBody Map<String, Boolean> newSettings) {
        try {
            adminControllerService.updateSettings(newSettings);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Ошибка: " + e.getMessage());
        }
        return ResponseEntity.ok("Настройки обновлены");
    }

    @GetMapping("/currentsettings")
    public Map<String, Boolean> getCurrentSettings() {
        Map<String, Boolean> settings;
        try {
            settings = adminControllerService.loadSettings();
        } catch (Exception e) {
            return Collections.emptyMap();
        }
        return settings;
    }
}
