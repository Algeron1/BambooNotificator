package com.rgs.bamboonotifier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.BugReportInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class BugReportService {

    private static final Logger logger = LoggerFactory.getLogger(BugReportService.class);
    private static final String PREFIX = "bugReport:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public BugReportService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public BugReportInfo create(String authorId, String authorName, String title, String description) {
        BugReportInfo report = new BugReportInfo();
        report.setId(UUID.randomUUID().toString());
        report.setTitle(title);
        report.setDescription(description);
        report.setAuthorId(authorId);
        report.setAuthorName(authorName);
        report.setCreatedAt(System.currentTimeMillis());
        report.setStatus("OPEN");
        try {
            redisTemplate.opsForValue().set(PREFIX + report.getId(), objectMapper.writeValueAsString(report));
        } catch (Exception e) {
            logger.error("Ошибка при сохранении баг-репорта: {}", e.getMessage());
            throw new RuntimeException("Не удалось сохранить баг-репорт");
        }
        return report;
    }

    public List<BugReportInfo> getAll() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys == null || keys.isEmpty()) return Collections.emptyList();
        return keys.stream()
                .map(key -> {
                    String json = redisTemplate.opsForValue().get(key);
                    if (json == null) return null;
                    try { return objectMapper.readValue(json, BugReportInfo.class); }
                    catch (Exception e) { return null; }
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingLong(BugReportInfo::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public void close(String id) {
        String key = PREFIX + id;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return;
        try {
            BugReportInfo report = objectMapper.readValue(json, BugReportInfo.class);
            report.setStatus("CLOSED");
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(report));
        } catch (Exception e) {
            logger.warn("Ошибка при закрытии баг-репорта {}: {}", id, e.getMessage());
        }
    }

    public void delete(String id) {
        redisTemplate.delete(PREFIX + id);
    }
}
