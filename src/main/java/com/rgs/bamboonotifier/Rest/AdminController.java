package com.rgs.bamboonotifier.Rest;

import com.rgs.bamboonotifier.sender.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller()
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final MessageSender messageSender;

    public AdminController(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    @GetMapping("/admin")
    public String getAdmin() {
        return "admin";
    }

    @PostMapping("/sendText")
    public ResponseEntity<String> sendTextMessage(@RequestBody String message) {
        if (message == null || message.isEmpty()) {
            return ResponseEntity.badRequest().body("Пустое сообщение");
        }
        try {
            messageSender.sendTextMessage(message);
            logger.info("Отправка сообщения через панель администратора успешна");
        } catch (Exception e) {
            logger.error("Ошибка при отправке сообщения через панель администратора: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Ошибка при отправке сообщения: " + e.getMessage());
        }
        return ResponseEntity.ok().body("успешно отправлено");
    }
}
