package com.rgs.bamboonotifier.Rest;

import com.rgs.bamboonotifier.DTO.CommentEntry;
import com.rgs.bamboonotifier.Entity.User;
import com.rgs.bamboonotifier.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;
    private final AuthController authController;

    public CommentController(CommentService commentService, AuthController authController) {
        this.commentService = commentService;
        this.authController = authController;
    }

    @GetMapping
    public ResponseEntity<List<CommentEntry>> getComments(@RequestParam String stand) {
        return ResponseEntity.ok(commentService.getComments(stand));
    }

    @PostMapping
    public ResponseEntity<?> addComment(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> body) {

        User user = authController.requireUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Требуется авторизация");

        String stand = body.getOrDefault("standName", "").trim();
        String text  = body.getOrDefault("text", "").trim();
        if (stand.isBlank() || text.isBlank()) {
            return ResponseEntity.badRequest().body("Стенд и текст не могут быть пустыми");
        }
        if (text.length() > 500) {
            return ResponseEntity.badRequest().body("Комментарий не может быть длиннее 500 символов");
        }

        String authorName = user.getFirstName() + " " + user.getLastName();
        CommentEntry entry = commentService.addComment(stand, user.getId(), authorName, text);
        return ResponseEntity.status(HttpStatus.CREATED).body(entry);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable String commentId,
            @RequestParam String stand,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        User user = authController.requireUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Требуется авторизация");

        boolean isAdmin = "ADMIN".equals(user.getRole());
        boolean deleted = commentService.deleteComment(commentId, stand, user.getId(), isAdmin);
        if (!deleted) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нет доступа или комментарий не найден");
        return ResponseEntity.ok().build();
    }
}
