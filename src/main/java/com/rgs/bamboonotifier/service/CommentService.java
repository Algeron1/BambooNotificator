package com.rgs.bamboonotifier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.CommentEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
public class CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentService.class);
    private static final String COMMENT_PREFIX = "comment:";
    private static final String INDEX_PREFIX = "comments:stand:";
    private static final int MAX_COMMENTS = 50;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CommentService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public CommentEntry addComment(String standName, String authorId, String authorName, String text) {
        CommentEntry entry = new CommentEntry();
        entry.setId(UUID.randomUUID().toString());
        entry.setStandName(standName);
        entry.setAuthorId(authorId);
        entry.setAuthorName(authorName);
        entry.setText(text.trim());
        entry.setCreatedAt(System.currentTimeMillis());

        try {
            String json = objectMapper.writeValueAsString(entry);
            redisTemplate.opsForValue().set(COMMENT_PREFIX + entry.getId(), json, Duration.ofDays(30));
            redisTemplate.opsForZSet().add(INDEX_PREFIX + standName, entry.getId(), entry.getCreatedAt());
            trimOldComments(standName);
        } catch (Exception e) {
            logger.error("Ошибка при сохранении комментария: {}", e.getMessage());
            throw new RuntimeException("Не удалось сохранить комментарий");
        }
        return entry;
    }

    public List<CommentEntry> getComments(String standName) {
        Set<String> ids = redisTemplate.opsForZSet().reverseRange(INDEX_PREFIX + standName, 0, 29);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();

        List<CommentEntry> result = new ArrayList<>();
        for (String id : ids) {
            String json = redisTemplate.opsForValue().get(COMMENT_PREFIX + id);
            if (json == null) continue;
            try {
                result.add(objectMapper.readValue(json, CommentEntry.class));
            } catch (Exception e) {
                logger.warn("Ошибка при разборе комментария {}: {}", id, e.getMessage());
            }
        }
        return result;
    }

    public boolean deleteComment(String commentId, String standName, String requestingUserId, boolean isAdmin) {
        String json = redisTemplate.opsForValue().get(COMMENT_PREFIX + commentId);
        if (json == null) return false;
        try {
            CommentEntry entry = objectMapper.readValue(json, CommentEntry.class);
            if (!isAdmin && !entry.getAuthorId().equals(requestingUserId)) return false;
            redisTemplate.delete(COMMENT_PREFIX + commentId);
            redisTemplate.opsForZSet().remove(INDEX_PREFIX + standName, commentId);
            return true;
        } catch (Exception e) {
            logger.error("Ошибка при удалении комментария {}: {}", commentId, e.getMessage());
            return false;
        }
    }

    public long getCommentCount(String standName) {
        Long size = redisTemplate.opsForZSet().size(INDEX_PREFIX + standName);
        return size != null ? size : 0;
    }

    private void trimOldComments(String standName) {
        Long size = redisTemplate.opsForZSet().size(INDEX_PREFIX + standName);
        if (size != null && size > MAX_COMMENTS) {
            Set<String> old = redisTemplate.opsForZSet().range(INDEX_PREFIX + standName, 0, size - MAX_COMMENTS - 1);
            if (old != null) {
                old.forEach(id -> {
                    redisTemplate.delete(COMMENT_PREFIX + id);
                    redisTemplate.opsForZSet().remove(INDEX_PREFIX + standName, id);
                });
            }
        }
    }
}
