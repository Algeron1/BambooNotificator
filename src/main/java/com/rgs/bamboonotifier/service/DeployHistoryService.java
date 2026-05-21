package com.rgs.bamboonotifier.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.DeployHistoryEntry;
import com.rgs.bamboonotifier.DTO.DeployResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DeployHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(DeployHistoryService.class);
    private static final String KEY_PREFIX = "bamboo:history:";
    private static final int MAX_HISTORY = 5;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public DeployHistoryService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public void push(String standName, DeployResult result, long timestamp) {
        try {
            DeployHistoryEntry entry = new DeployHistoryEntry();
            entry.setTimestamp(timestamp);
            entry.setState(result.getDeploymentState());
            entry.setLifeCycleState(result.getLifeCycleState());
            if (result.getDeploymentVersion() != null) {
                entry.setVersion(result.getDeploymentVersion().getName());
                entry.setBranch(result.getDeploymentVersion().getPlanBranchName());
                String author = result.getDeploymentVersion().getCreatorDisplayName();
                entry.setAuthor(author != null ? author : "Автодеплой");
            }
            String key = KEY_PREFIX + standName;
            redisTemplate.opsForList().leftPush(key, objectMapper.writeValueAsString(entry));
            redisTemplate.opsForList().trim(key, 0, MAX_HISTORY - 1);
        } catch (Exception e) {
            logger.warn("Ошибка при сохранении истории для {}: {}", standName, e.getMessage());
        }
    }

    public List<DeployHistoryEntry> get(String standName) {
        List<String> jsonList = redisTemplate.opsForList().range(KEY_PREFIX + standName, 0, MAX_HISTORY - 1);
        List<DeployHistoryEntry> result = new ArrayList<>();
        if (jsonList == null) return result;
        for (String json : jsonList) {
            try {
                result.add(objectMapper.readValue(json, DeployHistoryEntry.class));
            } catch (Exception e) {
                logger.warn("Ошибка при разборе записи истории: {}", e.getMessage());
            }
        }
        return result;
    }
}
