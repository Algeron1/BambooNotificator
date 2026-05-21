package com.rgs.bamboonotifier.Rest;

import com.rgs.bamboonotifier.DTO.UserInfo;
import com.rgs.bamboonotifier.Entity.User;
import com.rgs.bamboonotifier.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        try {
            String username  = str(body, "username");
            String firstName = str(body, "firstName");
            String lastName  = str(body, "lastName");
            String password  = str(body, "password");
            boolean reqAdmin = Boolean.TRUE.equals(body.get("requestAdmin"));

            if (username.isBlank() || firstName.isBlank() || lastName.isBlank() || password.isBlank()) {
                return ResponseEntity.badRequest().body("Заполните все поля");
            }
            UserInfo info = userService.register(username, firstName, lastName, password, reqAdmin);
            return ResponseEntity.status(HttpStatus.CREATED).body(info);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка регистрации");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String username = body.getOrDefault("username", "");
        String password = body.getOrDefault("password", "");
        Optional<String> token = userService.login(username, password);
        if (token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный логин или пароль");
        }
        Optional<User> user = userService.getUserByToken(token.get());
        if (user.isEmpty()) return ResponseEntity.internalServerError().build();
        return ResponseEntity.ok(Map.of("token", token.get(), "user", userService.toInfo(user.get())));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        if (token != null) userService.logout(token);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = requireUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Требуется авторизация");
        return ResponseEntity.ok(userService.toInfo(user));
    }

    @PostMapping("/heartbeat")
    public ResponseEntity<Void> heartbeat(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = requireUser(authHeader);
        if (user == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        userService.heartbeat(user.getId());
        return ResponseEntity.ok().build();
    }

    User requireUser(String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) return null;
        return userService.getUserByToken(token).orElse(null);
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) return authHeader.substring(7);
        return null;
    }

    private String str(Map<String, Object> body, String key) {
        Object val = body.get(key);
        return val != null ? val.toString().trim() : "";
    }
}
