package com.rgs.bamboonotifier.Rest;

import com.rgs.bamboonotifier.DTO.BugReportInfo;
import com.rgs.bamboonotifier.Entity.User;
import com.rgs.bamboonotifier.service.BugReportService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/bug-reports")
public class BugReportController {

    private final BugReportService bugReportService;
    private final AuthController authController;

    public BugReportController(BugReportService bugReportService, AuthController authController) {
        this.bugReportService = bugReportService;
        this.authController = authController;
    }

    @PostMapping
    public ResponseEntity<?> create(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {

        User user = authController.requireUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Требуется авторизация");

        String title       = body.getOrDefault("title", "").trim();
        String description = body.getOrDefault("description", "").trim();

        if (title.isBlank())        return ResponseEntity.badRequest().body("Краткое описание обязательно");
        if (title.length() > 200)   return ResponseEntity.badRequest().body("Заголовок слишком длинный");
        if (description.length() > 2000) return ResponseEntity.badRequest().body("Описание слишком длинное");

        try {
            String authorName = user.getFirstName() + " " + user.getLastName();
            BugReportInfo report = bugReportService.create(user.getId(), authorName, title, description);
            return ResponseEntity.status(HttpStatus.CREATED).body(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка при сохранении");
        }
    }
}
