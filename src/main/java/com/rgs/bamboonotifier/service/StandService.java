package com.rgs.bamboonotifier.service;

import com.rgs.bamboonotifier.config.BambooProperties;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class StandService {

    private static final Logger logger = LoggerFactory.getLogger(StandService.class);
    private static final String STANDS_KEY = "bamboo:stands";

    private final RedisTemplate<String, String> redisTemplate;
    private final BambooProperties bambooProperties;

    public StandService(RedisTemplate<String, String> redisTemplate, BambooProperties bambooProperties) {
        this.redisTemplate = redisTemplate;
        this.bambooProperties = bambooProperties;
    }

    @PostConstruct
    public void syncFromConfig() {
        Map<String, String> ids = bambooProperties.getDeploymentIds();
        if (ids != null && !ids.isEmpty()) {
            redisTemplate.opsForHash().putAll(STANDS_KEY, ids);
            logger.info("Стенды из конфига загружены в Redis: {} шт.", ids.size());
        }
    }

    public Map<String, String> getDeploymentIds() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(STANDS_KEY);
        Map<String, String> result = new LinkedHashMap<>();
        entries.forEach((k, v) -> result.put(k.toString(), v.toString()));
        return result;
    }

    public void addStand(String environmentId, String displayName) {
        Objects.requireNonNull(environmentId, "environmentId не может быть null");
        Objects.requireNonNull(displayName, "displayName не может быть null");
        redisTemplate.opsForHash().put(STANDS_KEY, environmentId, displayName);
        logger.info("Добавлен стенд: {} -> {}", environmentId, displayName);
    }

    public void removeStand(String environmentId) {
        Objects.requireNonNull(environmentId, "environmentId не может быть null");
        redisTemplate.opsForHash().delete(STANDS_KEY, environmentId);
        logger.info("Удалён стенд: {}", environmentId);
    }

    public boolean exists(String environmentId) {
        Objects.requireNonNull(environmentId, "environmentId не может быть null");
        return Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(STANDS_KEY, environmentId));
    }
}
