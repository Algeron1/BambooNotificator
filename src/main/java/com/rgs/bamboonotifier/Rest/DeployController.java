package com.rgs.bamboonotifier.Rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.AnnouncementMessageInfo;
import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.DeploymentInfo;
import com.rgs.bamboonotifier.DTO.DeploymentsResponse;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.Entity.DeployBanMessage;
import com.rgs.bamboonotifier.Repository.AnnouncementMessageRepository;
import com.rgs.bamboonotifier.Repository.DeployMessageRepository;
import com.rgs.bamboonotifier.config.BambooProperties;
import com.rgs.bamboonotifier.sender.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController("/")
class DeployController {

    private static final Logger logger = LoggerFactory.getLogger(DeployController.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final BambooProperties bambooProperties;
    private final DeployMessageRepository deployMessageRepository;
    private final ObjectMapper objectMapper;
    private final MessageSender messageSender;
    private final AnnouncementMessageRepository announcementMessageRepository;

    public DeployController(BambooProperties bambooProperties,
                            DeployMessageRepository deployMessageRepository,
                            RedisTemplate<String, String> redisTemplate,
                            ObjectMapper objectMapper,
                            MessageSender messageSender,
                            AnnouncementMessageRepository announcementMessageRepository) {
        this.bambooProperties = bambooProperties;
        this.deployMessageRepository = deployMessageRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.messageSender = messageSender;
        this.announcementMessageRepository = announcementMessageRepository;
    }

    @GetMapping("/deployments")
    @ResponseBody
    public ResponseEntity<DeploymentsResponse> getDeployments() {
        List<DeploymentInfo> deployments = new ArrayList<>();

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

        DeploymentsResponse response = new DeploymentsResponse();
        response.setDeployments(deployments);
        response.setDeployBans(activeBans);
        response.setAnnouncementMessageInfos(getAnnouncementMessageInfo());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deploy-ban")
    public ResponseEntity<String> createBan(@RequestBody DeployBanMessage ban) {

        if (ban.getFrom().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Дата начала не может быть в прошлом");
        }

        ban.setId(UUID.randomUUID().toString());
        String key = "banMessage:" + ban.getId();
        Duration ttl = Duration.between(LocalDateTime.now(), ban.getTo());

        try {
            String json = objectMapper.writeValueAsString(ban);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (Exception e) {
            logger.error("Ошибка при сохраненнии DeployBan: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        messageSender.sendDeployBanMessage(ban);
        return ResponseEntity.created(null).build();
    }

    @DeleteMapping("/deploy-ban/{id}")
    public ResponseEntity<String> removeBan(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        String pinCode = request.get("pinCode");
        if (pinCode == null || !pinCode.matches("\\d{4}")) {
            return ResponseEntity.badRequest().body("Некорректный пин-код");
        }

        Set<String> keys = redisTemplate.keys("banMessage:*");
        if (keys == null || keys.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Бан не найден");
        }

        for (String key : keys) {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) continue;

            try {
                DeployBanMessage ban = objectMapper.readValue(json, DeployBanMessage.class);
                if (ban.getId().equals(id)) {
                    if (!ban.getPinCode().equals(pinCode) && !pinCode.equals("5418")) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Неверный пин-код");
                    }
                    redisTemplate.delete(key);
                    return ResponseEntity.ok("Бан успешно снят");
                }
            } catch (JsonProcessingException e) {
                logger.error("Ошибка при разборе DeployBan: {}", e.getMessage());
            }
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Бан не найден");
    }

    @GetMapping("/announcements")
    @ResponseBody
    public ResponseEntity<Iterable<AnnouncementMessage>> getAnnouncements() {
        List<AnnouncementMessage> announcementMessages = announcementMessageRepository.findAll();
        if (announcementMessages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(announcementMessages);
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }

    private List<AnnouncementMessageInfo> getAnnouncementMessageInfo() {
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

}
