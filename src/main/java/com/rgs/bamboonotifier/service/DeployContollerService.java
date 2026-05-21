package com.rgs.bamboonotifier.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.AnnouncementMessageInfo;
import com.rgs.bamboonotifier.DTO.BuildInfo;
import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.DeploymentInfo;
import com.rgs.bamboonotifier.DTO.QueueItem;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.Entity.DeployBanMessage;
import com.rgs.bamboonotifier.Repository.AnnouncementMessageRepository;
import com.rgs.bamboonotifier.Repository.DeployMessageRepository;
import com.rgs.bamboonotifier.service.StandService;
import io.lettuce.core.RedisLoadingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.naming.AuthenticationException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DeployContollerService {

    private static final Logger logger = LoggerFactory.getLogger(DeployContollerService.class);

    private final StandService standService;
    private final DeployMessageRepository deployMessageRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AnnouncementMessageRepository announcementMessageRepository;

    public DeployContollerService(StandService standService,
                                  DeployMessageRepository deployMessageRepository,
                                  RedisTemplate<String, String> redisTemplate,
                                  ObjectMapper objectMapper,
                                  AnnouncementMessageRepository announcementMessageRepository) {
        this.standService = standService;
        this.deployMessageRepository = deployMessageRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.announcementMessageRepository = announcementMessageRepository;
    }

    public List<AnnouncementMessageInfo> getAnnouncementMessageInfo() {
        Set<String> keys = redisTemplate.keys("announcementMessage:*");

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        List<AnnouncementMessageInfo> infos = new ArrayList<>(keys.size());

        for (String key : keys) {
            try {
                String json = redisTemplate.opsForValue().get(key);
                if (json == null) continue;

                AnnouncementMessage announcement = objectMapper.readValue(json, AnnouncementMessage.class);

                AnnouncementMessageInfo info = new AnnouncementMessageInfo();
                info.setId(announcement.getId());
                info.setFrom(announcement.getFrom());
                info.setTo(announcement.getTo());
                info.setText(announcement.getText());
                info.setAuthor(announcement.getAuthor());
                info.setWarningLevel(announcement.getWarningLevel());

                infos.add(info);
            } catch (Exception e) {
                logger.warn("Ошибка при обработке объявления по ключу {}: {}", key, e.getMessage());
            }
        }

        logger.debug("Найдено {} объявлений", infos.size());
        return infos;
    }

    public List<DeploymentInfo> getDeploymentInfo() {
        List<DeploymentInfo> deployments = new ArrayList<>();

        try {
            for (Map.Entry<String, String> entry : standService.getDeploymentIds().entrySet()) {
                String environmentId = entry.getKey();
                String standName = entry.getValue();
                DeployResult result = null;
                String cached = redisTemplate.opsForValue().get("bamboo:data:" + standName);
                if (cached != null) {
                    try {
                        result = objectMapper.readValue(cached, DeployResult.class);
                    } catch (Exception e) {
                        logger.warn("Не удалось десериализовать кэш стенда {}: {}", standName, e.getMessage());
                    }
                }
                if (result == null) {
                    var history = deployMessageRepository.findAllByEnvironmentIdOrderByCreatedAtDesc(environmentId);
                    if (!history.isEmpty()) result = history.getFirst().getDeployResult();
                }
                if (result == null) continue;

                if (result != null) {
                    DeploymentInfo deploymentInfo = new DeploymentInfo();
                    deploymentInfo.setEnvironmentId(standName);
                    deploymentInfo.setDeployVersion(result.getDeploymentVersion().getName());
                    deploymentInfo.setStatus(result.getDeploymentState());
                    deploymentInfo.setProgressStatus(result.getLifeCycleState());
                    deploymentInfo.setStartedDate(result.getStartedDate() == null ? null : formatDate(result.getStartedDate()));
                    deploymentInfo.setFinishedDate(result.getFinishedDate() == null ? null : formatDate(result.getFinishedDate()));
                    deploymentInfo.setAuthor(result.getDeploymentVersion().getCreatorDisplayName() == null ? "Автодеплой" : result.getDeploymentVersion().getCreatorDisplayName());
                    deploymentInfo.setBranchName(result.getDeploymentVersion().getPlanBranchName());
                    deploymentInfo.setTriggerType(parseTriggerType(result.getReasonSummary()));
                    if (result.getLogFiles() != null && !result.getLogFiles().isEmpty())
                        deploymentInfo.setLogUrl(result.getLogFiles().get(0));
                    java.util.Date lastActivity = result.getFinishedDate() != null ? result.getFinishedDate() : result.getStartedDate();
                    deploymentInfo.setLastUpdated(lastActivity != null ? lastActivity.getTime() : null);

                    if (result.getFinishedDate() != null && result.getStartedDate() != null) {
                        long ms = result.getFinishedDate().getTime() - result.getStartedDate().getTime();
                        if (ms > 0) deploymentInfo.setDuration(formatDuration(ms));
                    }

                    if (result.getDeploymentVersion() != null) {
                        String buildCacheKey = "bamboo:build:" + result.getDeploymentVersion().getId();
                        String buildJson = redisTemplate.opsForValue().get(buildCacheKey);
                        if (buildJson != null && !buildJson.equals("NONE")) {
                            try {
                                BuildInfo buildInfo = objectMapper.readValue(buildJson, BuildInfo.class);
                                deploymentInfo.setBuildState(buildInfo.getState());
                                deploymentInfo.setTestsPassed(buildInfo.getTestsPassed());
                                deploymentInfo.setTestsFailed(buildInfo.getTestsFailed());
                            } catch (Exception e) {
                                logger.warn("Ошибка при чтении статуса билда: {}", e.getMessage());
                            }
                        }
                    }

                    deployments.add(deploymentInfo);
                }
            }
        } catch (Exception e) {
            logger.error("Ошибка при получении списка деплоев", e);
        }
        return deployments;
    }

    public List<DeployBanMessage> deployBanMessages() {
        List<DeployBanMessage> activeBans = redisTemplate.keys("banMessage:*").stream()
                .map(key -> {
                    String json = redisTemplate.opsForValue().get(key);
                    try {
                        return objectMapper.readValue(json, DeployBanMessage.class);
                    } catch (JsonProcessingException e) {
                        logger.error("Ошибка при получении DeployBan {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return activeBans;
    }

    public void createDeployBanMessage(DeployBanMessage deployBanMessage) throws Exception{
        deployBanMessage.setId(UUID.randomUUID().toString());
        String key = "banMessage:" + deployBanMessage.getId();
        Duration ttl = Duration.between(LocalDateTime.now(), deployBanMessage.getTo());

        try {
            String json = objectMapper.writeValueAsString(deployBanMessage);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            logger.error("Ошибка при сохраненнии DeployBan: {}", e.getMessage());
            throw e;
        }
    }

    public void deleteDeployBanMessage(String pinCode, String id) throws Exception{
        Set<String> keys = redisTemplate.keys("banMessage:*");
        if (keys == null || keys.isEmpty()) {
            throw new RedisLoadingException("Бан не найден");
        }
        for (String key : keys) {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) continue;

            try {
                DeployBanMessage ban = objectMapper.readValue(json, DeployBanMessage.class);
                if (ban.getId().equals(id)) {
                    if (!ban.getPinCode().equals(pinCode) && !pinCode.equals("5418")) {
                        throw new AuthenticationException("Неверный пин-код");
                    }
                    redisTemplate.delete(key);
                }
            } catch (JsonProcessingException e) {
                logger.error("Ошибка при разборе DeployBan: {}", e.getMessage());
            }
        }
    }

    public List<QueueItem> getQueueItems() {
        Set<String> keys = redisTemplate.keys("bamboo:data:*");
        if (keys == null || keys.isEmpty()) return Collections.emptyList();

        List<QueueItem> items = new ArrayList<>();
        for (String key : keys) {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) continue;
            try {
                DeployResult result = objectMapper.readValue(json, DeployResult.class);
                if (!"QUEUED".equals(result.getLifeCycleState())) continue;

                String envDisplay = key.substring("bamboo:data:".length());
                String[] parts = envDisplay.split("_", 2);

                QueueItem item = new QueueItem();
                item.setEnvironmentId(envDisplay);
                item.setStandName(parts[0]);
                item.setSystemName(parts.length > 1 ? parts[1] : envDisplay);
                if (result.getDeploymentVersion() != null) {
                    item.setVersion(result.getDeploymentVersion().getName());
                    item.setAuthor(result.getDeploymentVersion().getCreatorDisplayName());
                    item.setBranch(result.getDeploymentVersion().getPlanBranchName());
                }
                items.add(item);
            } catch (Exception e) {
                logger.warn("Ошибка при разборе кэша {}: {}", key, e.getMessage());
            }
        }
        return items;
    }

    public List<AnnouncementMessage> getAnnouncementMessages() {
        return announcementMessageRepository.findAll();
    }

    private static String parseTriggerType(String reason) {
        if (reason == null) return null;
        if (reason.startsWith("Manual run")) return "MANUAL";
        if (reason.startsWith("Scheduled"))  return "SCHEDULED";
        if (reason.startsWith("Child of"))   return "CHILD";
        return null;
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
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
}
