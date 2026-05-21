package com.rgs.bamboonotifier.service;

import com.rgs.bamboonotifier.DTO.UserInfo;
import com.rgs.bamboonotifier.Entity.User;
import com.rgs.bamboonotifier.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.StreamSupport;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static final String SESSION_PREFIX = "session:";
    private static final String HEARTBEAT_KEY = "users:heartbeat";
    private static final long ONLINE_WINDOW_MS = 5 * 60 * 1000L;

    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public UserService(UserRepository userRepository, RedisTemplate<String, String> redisTemplate) {
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }

    public UserInfo register(String username, String firstName, String lastName, String password, boolean requestAdmin) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Логин не может быть пустым");
        if (password == null || password.length() < 4) throw new IllegalArgumentException("Пароль должен быть не менее 4 символов");
        if (userRepository.findByUsername(username.trim()).isPresent()) {
            throw new IllegalStateException("Пользователь с таким логином уже существует");
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(username.trim().toLowerCase());
        user.setFirstName(firstName.trim());
        user.setLastName(lastName.trim());
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setStatus(requestAdmin ? "PENDING_ADMIN" : "ACTIVE");
        user.setCreatedAt(System.currentTimeMillis());
        user.setLastSeenAt(System.currentTimeMillis());

        userRepository.save(user);
        logger.info("Зарегистрирован пользователь: {} ({})", user.getUsername(), user.getStatus());
        return toInfo(user);
    }

    public Optional<String> login(String username, String password) {
        Optional<User> opt = userRepository.findByUsername(username.trim().toLowerCase());
        if (opt.isEmpty()) return Optional.empty();
        User user = opt.get();
        if (!"ACTIVE".equals(user.getStatus()) && !"PENDING_ADMIN".equals(user.getStatus())) return Optional.empty();
        if (!passwordEncoder.matches(password, user.getPasswordHash())) return Optional.empty();

        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(SESSION_PREFIX + token, user.getId(), Duration.ofDays(1));
        heartbeat(user.getId());
        logger.info("Вход пользователя: {}", user.getUsername());
        return Optional.of(token);
    }

    public void logout(String token) {
        redisTemplate.delete(SESSION_PREFIX + token);
    }

    public Optional<User> getUserByToken(String token) {
        if (token == null) return Optional.empty();
        String userId = redisTemplate.opsForValue().get(SESSION_PREFIX + token);
        if (userId == null) return Optional.empty();
        return userRepository.findById(userId);
    }

    public void heartbeat(String userId) {
        redisTemplate.opsForZSet().add(HEARTBEAT_KEY, userId, System.currentTimeMillis());
        userRepository.findById(userId).ifPresent(u -> {
            u.setLastSeenAt(System.currentTimeMillis());
            userRepository.save(u);
        });
    }

    public UserInfo toInfo(User user) {
        UserInfo info = new UserInfo();
        info.setId(user.getId());
        info.setUsername(user.getUsername());
        info.setFirstName(user.getFirstName());
        info.setLastName(user.getLastName());
        info.setRole(user.getRole());
        info.setStatus(user.getStatus());
        info.setCreatedAt(user.getCreatedAt());
        info.setOnline(System.currentTimeMillis() - user.getLastSeenAt() < ONLINE_WINDOW_MS);
        return info;
    }

    public List<UserInfo> getAllUsers() {
        return StreamSupport.stream(userRepository.findAll().spliterator(), false)
                .sorted(Comparator.comparingLong(User::getCreatedAt).reversed())
                .map(this::toInfo)
                .toList();
    }

    public List<UserInfo> getPendingAdminRequests() {
        return userRepository.findByStatus("PENDING_ADMIN").stream()
                .map(this::toInfo).toList();
    }

    public void approveAdmin(String userId) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setRole("ADMIN");
            u.setStatus("ACTIVE");
            userRepository.save(u);
            logger.info("Пользователь {} получил права администратора", u.getUsername());
        });
    }

    public void setRole(String userId, String role) {
        userRepository.findById(userId).ifPresent(u -> {
            u.setRole(role);
            if ("ADMIN".equals(role)) u.setStatus("ACTIVE");
            userRepository.save(u);
            logger.info("Роль пользователя {} изменена на {}", u.getUsername(), role);
        });
    }

    public void deleteUser(String userId) {
        userRepository.deleteById(userId);
        logger.info("Пользователь {} удалён", userId);
    }

    public long getTotalCount() {
        return userRepository.count();
    }

    public long getOnlineCount() {
        long from = System.currentTimeMillis() - ONLINE_WINDOW_MS;
        Long count = redisTemplate.opsForZSet().count(HEARTBEAT_KEY, from, Double.MAX_VALUE);
        return count != null ? count : 0;
    }
}
