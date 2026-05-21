package com.rgs.bamboonotifier.Sheduler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.BuildInfo;
import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.DeploymentInfo;
import com.rgs.bamboonotifier.sender.MessageSender;
import com.rgs.bamboonotifier.service.BambooService;
import com.rgs.bamboonotifier.service.DeployHistoryService;
import com.rgs.bamboonotifier.service.StandService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class BambooScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BambooScheduler.class);

    private final BambooService bambooService;
    private final StandService standService;
    private final MessageSender messageSender;
    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final DeployHistoryService deployHistoryService;
    private final ObjectMapper objectMapper;

    public BambooScheduler(BambooService bambooService,
                           MessageSender sender,
                           StandService standService,
                           RedisTemplate<String, String> redisTemplate,
                           SimpMessagingTemplate messagingTemplate,
                           DeployHistoryService deployHistoryService,
                           ObjectMapper objectMapper) {
        this.bambooService = bambooService;
        this.messageSender = sender;
        this.standService = standService;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.deployHistoryService = deployHistoryService;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedRate = 30000)
    public void checkDeploymentStatuses() {
        for (Map.Entry<String, String> entry : standService.getDeploymentIds().entrySet()) {

            String environmentId = entry.getKey();
            String standName = entry.getValue();

            try {
                DeployResult status = bambooService.getDeploymentStatus(environmentId);
                if (status == null) {
                    logger.warn("Не удалось получить статус для стенда {}", standName);
                    continue;
                }

                try {
                    redisTemplate.opsForValue().set("bamboo:data:" + standName, objectMapper.writeValueAsString(status));
                } catch (Exception e) {
                    logger.warn("Не удалось сохранить данные стенда {}: {}", standName, e.getMessage());
                }

                if (status.getDeploymentVersion() != null) {
                    String planResultKey = status.getDeploymentVersion().getPlanResultKey();
                    long versionId = status.getDeploymentVersion().getId();
                    String buildCacheKey = "bamboo:build:" + versionId;
                    if (!Boolean.TRUE.equals(redisTemplate.hasKey(buildCacheKey))) {
                        if (planResultKey == null) {
                            planResultKey = bambooService.getPlanResultKeyFromDeployResult(status.getId());
                        }
                        if (planResultKey != null) {
                            cacheBuildStatus(planResultKey, buildCacheKey);
                        } else {
                            redisTemplate.opsForValue().set(buildCacheKey, "NONE");
                        }
                    }
                }

                String stateKey = "bamboo:state:" + standName;
                String currentState = status.getId() + ":" + status.getDeploymentState() + ":" + status.getLifeCycleState();
                String lastState = redisTemplate.opsForValue().get(stateKey);

                if (!currentState.equals(lastState)) {
                    long now = System.currentTimeMillis();
                    messageSender.sendMessage(status, standName, environmentId);
                    messagingTemplate.convertAndSend("/topic/deployments", buildUpdate(status, standName, now));
                    deployHistoryService.push(standName, status, now);
                    redisTemplate.opsForValue().set(stateKey, currentState);
                    logger.info("Отправлено уведомление по стенду {}", standName);
                }
            } catch (Exception e) {
                logger.error("Ошибка при проверке стенда {}: {}", standName, e.getMessage());
            }
        }
    }

    private String formatDuration(long ms) {
        long seconds = ms / 1000;
        if (seconds < 60) return seconds + " сек";
        long minutes = seconds / 60;
        long secs = seconds % 60;
        if (minutes < 60) return minutes + " мин " + secs + " сек";
        long hours = minutes / 60;
        long mins = minutes % 60;
        return hours + " ч " + mins + " мин";
    }

    private void cacheBuildStatus(String planResultKey, String cacheKey) {
        try {
            var result = bambooService.getBuildResult(planResultKey);
            if (result.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, "NONE");
                return;
            }
            BuildInfo info = new BuildInfo();
            info.setState(result.get().getState());
            info.setTestsPassed(result.get().getSuccessfulTestCount());
            info.setTestsFailed(result.get().getFailedTestCount());
            redisTemplate.opsForValue().set(cacheKey, objectMapper.writeValueAsString(info));
            logger.debug("Закэширован статус билда: {}", cacheKey);
        } catch (Exception e) {
            logger.warn("Ошибка кэширования статуса билда {}: {}", planResultKey, e.getMessage());
        }
    }

    @NonNull
    private DeploymentInfo buildUpdate(DeployResult status, String standName, long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        DeploymentInfo info = new DeploymentInfo();
        info.setEnvironmentId(standName);
        info.setStatus(status.getDeploymentState());
        info.setProgressStatus(status.getLifeCycleState());
        info.setDeployVersion(status.getDeploymentVersion().getName());
        info.setBranchName(status.getDeploymentVersion().getPlanBranchName());
        String author = status.getDeploymentVersion().getCreatorDisplayName();
        info.setAuthor(author != null ? author : "Автодеплой");
        if (status.getStartedDate() != null) info.setStartedDate(sdf.format(status.getStartedDate()));
        if (status.getFinishedDate() != null) {
            info.setFinishedDate(sdf.format(status.getFinishedDate()));
            if (status.getStartedDate() != null) {
                long ms = status.getFinishedDate().getTime() - status.getStartedDate().getTime();
                if (ms > 0) info.setDuration(formatDuration(ms));
            }
        }
        info.setTriggerType(parseTriggerType(status.getReasonSummary()));
        if (status.getLogFiles() != null && !status.getLogFiles().isEmpty())
            info.setLogUrl(status.getLogFiles().get(0));
        info.setLastUpdated(timestamp);
        return info;
    }

    private static String parseTriggerType(String reason) {
        if (reason == null) return null;
        if (reason.startsWith("Manual run")) return "MANUAL";
        if (reason.startsWith("Scheduled"))  return "SCHEDULED";
        if (reason.startsWith("Child of"))   return "CHILD";
        return null;
    }
}
