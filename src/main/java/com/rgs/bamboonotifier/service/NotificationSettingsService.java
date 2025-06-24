package com.rgs.bamboonotifier.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationSettingsService {
    private final RedisTemplate<String, String> redisTemplate;

    @Autowired
    public NotificationSettingsService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isTelegramEnabled() {
        String value = redisTemplate.opsForValue().get("notification.telegram.enabled");
        return value == null || Boolean.parseBoolean(value);
    }

    public boolean isPachkaEnabled() {
        String value = redisTemplate.opsForValue().get("notification.pachka.enabled");
        return value == null || Boolean.parseBoolean(value);
    }

    public void updateSetting(String key, boolean enabled) {
        redisTemplate.opsForValue().set(key, String.valueOf(enabled));
    }
}

