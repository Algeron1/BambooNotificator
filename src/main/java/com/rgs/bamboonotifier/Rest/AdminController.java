package com.rgs.bamboonotifier.Rest;

import com.rgs.bamboonotifier.DTO.AnnouncementMessageInfo;
import com.rgs.bamboonotifier.DTO.BugReportInfo;
import com.rgs.bamboonotifier.DTO.UserInfo;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.config.InMemoryLogAppender;
import com.rgs.bamboonotifier.config.WebSocketEventListener;
import com.rgs.bamboonotifier.service.AdminControllerService;
import com.rgs.bamboonotifier.service.BugReportService;
import com.rgs.bamboonotifier.service.StandService;
import com.rgs.bamboonotifier.service.UserService;
import io.lettuce.core.RedisException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController()
@RequestMapping("/admin")
public class AdminController {

    private final AdminControllerService adminControllerService;
    private final StandService standService;
    private final UserService userService;
    private final BugReportService bugReportService;
    private final InMemoryLogAppender logAppender;
    private final WebSocketEventListener wsEventListener;

    public AdminController(AdminControllerService adminControllerService,
                           StandService standService,
                           UserService userService,
                           BugReportService bugReportService,
                           InMemoryLogAppender logAppender,
                           WebSocketEventListener wsEventListener) {
        this.adminControllerService = adminControllerService;
        this.standService = standService;
        this.userService = userService;
        this.bugReportService = bugReportService;
        this.logAppender = logAppender;
        this.wsEventListener = wsEventListener;
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

    @GetMapping("/stands")
    public ResponseEntity<Map<String, String>> getStands() {
        return ResponseEntity.ok(standService.getDeploymentIds());
    }

    @PostMapping("/stands")
    public ResponseEntity<String> addStand(@RequestBody Map<String, String> body) {
        String environmentId = body.get("environmentId");
        String displayName = body.get("displayName");
        if (environmentId == null || environmentId.isBlank() || displayName == null || displayName.isBlank()) {
            return ResponseEntity.badRequest().body("Заполните все поля");
        }
        if (standService.exists(environmentId.trim())) {
            return ResponseEntity.badRequest().body("Стенд с таким ID уже существует");
        }
        standService.addStand(environmentId.trim(), displayName.trim());
        return ResponseEntity.ok("Стенд добавлен");
    }

    @DeleteMapping("/stands/{environmentId}")
    public ResponseEntity<String> removeStand(@PathVariable String environmentId) {
        if (!standService.exists(environmentId)) {
            return ResponseEntity.status(404).body("Стенд не найден");
        }
        standService.removeStand(environmentId);
        return ResponseEntity.ok("Стенд удалён");
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserInfo>> getUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/pending")
    public ResponseEntity<List<UserInfo>> getPendingAdmins() {
        return ResponseEntity.ok(userService.getPendingAdminRequests());
    }

    @PostMapping("/users/{userId}/approve")
    public ResponseEntity<String> approveAdmin(@PathVariable String userId) {
        userService.approveAdmin(userId);
        return ResponseEntity.ok("Права администратора выданы");
    }

    @PostMapping("/users/{userId}/role")
    public ResponseEntity<String> setUserRole(@PathVariable String userId, @RequestBody Map<String, String> body) {
        String role = body.get("role");
        if (!"ADMIN".equals(role) && !"USER".equals(role)) {
            return ResponseEntity.badRequest().body("Недопустимая роль");
        }
        userService.setRole(userId, role);
        return ResponseEntity.ok("Роль изменена");
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok("Пользователь удалён");
    }

    @GetMapping("/logs")
    public ResponseEntity<List<InMemoryLogAppender.LogEntry>> getLogs(
            @RequestParam(defaultValue = "ALL") String level) {
        List<InMemoryLogAppender.LogEntry> entries = logAppender.getEntries();
        if (!"ALL".equals(level)) {
            entries = entries.stream().filter(e -> e.level().equals(level)).toList();
        }
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/bug-reports")
    public ResponseEntity<List<BugReportInfo>> getBugReports() {
        return ResponseEntity.ok(bugReportService.getAll());
    }

    @PatchMapping("/bug-reports/{id}/close")
    public ResponseEntity<String> closeBugReport(@PathVariable String id) {
        bugReportService.close(id);
        return ResponseEntity.ok("Баг-репорт закрыт");
    }

    @DeleteMapping("/bug-reports/{id}")
    public ResponseEntity<String> deleteBugReport(@PathVariable String id) {
        bugReportService.delete(id);
        return ResponseEntity.ok("Баг-репорт удалён");
    }

    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Long>> getUserStats() {
        return ResponseEntity.ok(Map.of(
                "total", userService.getTotalCount(),
                "online", userService.getOnlineCount(),
                "connections", (long) wsEventListener.getConnectionCount()
        ));
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
