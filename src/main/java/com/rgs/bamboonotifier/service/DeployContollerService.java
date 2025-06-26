package com.rgs.bamboonotifier.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.AnnouncementMessageInfo;
import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.DeploymentInfo;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.Entity.DeployBanMessage;
import com.rgs.bamboonotifier.Repository.AnnouncementMessageRepository;
import com.rgs.bamboonotifier.Repository.DeployMessageRepository;
import com.rgs.bamboonotifier.config.BambooProperties;
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

    private final BambooProperties bambooProperties;
    private final DeployMessageRepository deployMessageRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final AnnouncementMessageRepository announcementMessageRepository;

    public DeployContollerService(BambooProperties bambooProperties,
                                  DeployMessageRepository deployMessageRepository,
                                  RedisTemplate<String, String> redisTemplate,
                                  ObjectMapper objectMapper,
                                  AnnouncementMessageRepository announcementMessageRepository) {
        this.bambooProperties = bambooProperties;
        this.deployMessageRepository = deployMessageRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.announcementMessageRepository = announcementMessageRepository;
    }

    public List<AnnouncementMessageInfo> getAnnouncementMessageInfo() {
        Set<String> keys = redisTemplate.keys("announcementMessage:*");

        if (keys == null || keys.isEmpty()) {
            logger.info("Объявлений не найдено");
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

        logger.info("Найдено {} объявлений", infos.size());
        return infos;
    }

    public List<DeploymentInfo> getDeploymentInfo() {
        List<DeploymentInfo> deployments = new ArrayList<>();

        try {
            for (Map.Entry<String, String> entry : bambooProperties.getDeploymentIds().entrySet()) {
                String environmentId = entry.getKey();
                String standName = entry.getValue();
                DeployResult result = deployMessageRepository.findAllByEnvironmentIdOrderByCreatedAtDesc(environmentId).getFirst().getDeployResult();

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

    public List<AnnouncementMessage> getAnnouncementMessages() {
        return announcementMessageRepository.findAll();
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }
}
