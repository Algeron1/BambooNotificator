package com.rgs.bamboonotifier.Rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.DeploymentInfo;
import com.rgs.bamboonotifier.DTO.DeploymentsResponse;
import com.rgs.bamboonotifier.Entity.DeployBan;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.Repository.DeployMessageRepository;
import com.rgs.bamboonotifier.config.BambooProperties;
import com.rgs.bamboonotifier.sender.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
class FrontController {

    private static final Logger logger = LoggerFactory.getLogger(FrontController.class);

    private final RedisTemplate<String, String> redisTemplate;
    private final BambooProperties bambooProperties;
    private final DeployMessageRepository deployMessageRepository;
    private final ObjectMapper objectMapper;
    private final MessageSender messageSender;

    public FrontController(BambooProperties bambooProperties,
                           DeployMessageRepository deployMessageRepository,
                           RedisTemplate<String, String> redisTemplate,
                           ObjectMapper objectMapper,
                           MessageSender messageSender) {
        this.bambooProperties = bambooProperties;
        this.deployMessageRepository = deployMessageRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.messageSender = messageSender;
    }

    @GetMapping("/deployments")
    @ResponseBody
    public ResponseEntity<DeploymentsResponse> getDeployments() {
        List<DeploymentInfo> deployments = new ArrayList<>();

        for (Map.Entry<String, String> entry : bambooProperties.getDeploymentIds().entrySet()) {
            String environmentId = entry.getKey();
            String standName = entry.getValue();

            DeployResult result = deployMessageRepository.findFirstByEnvironmentIdOrderByCreatedAtDesc(environmentId).getDeployResult();

            if (result != null) {
                DeploymentInfo deploymentInfo = new DeploymentInfo();
                deploymentInfo.setEnvironmentId(standName);
                deploymentInfo.setDeployVersion(result.getDeploymentVersion().getName());
                deploymentInfo.setStatus(result.getDeploymentState());
                deploymentInfo.setStartedDate(result.getStartedDate() == null ? null : formatDate(result.getStartedDate()));
                deploymentInfo.setFinishedDate(result.getFinishedDate() == null ? null : formatDate(result.getFinishedDate()));
                deploymentInfo.setAuthor(result.getDeploymentVersion().getCreatorDisplayName() == null ? "Автодеплой" : result.getDeploymentVersion().getCreatorDisplayName());
                deploymentInfo.setBranchName(result.getDeploymentVersion().getPlanBranchName());
                deployments.add(deploymentInfo);
            }
        }

        List<DeployBan> activeBans = redisTemplate.keys("banMessage:*").stream()
                .map(key -> {
                    String json = redisTemplate.opsForValue().get(key);
                    try {
                        return objectMapper.readValue(json, DeployBan.class);
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

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deploy-ban")
    public ResponseEntity<Void> createBan(@RequestBody DeployBan ban) {

        String key = "banMessage:" + ban.getStandName() + ":" + UUID.randomUUID();
        Duration ttl = Duration.between(LocalDateTime.now(), ban.getTo());

        try {
            String json = objectMapper.writeValueAsString(ban);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException e) {
            logger.error("Ошибка при сохраненнии DeployBan: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }

        messageSender.sendDeployBanMessage(ban);
        return ResponseEntity.created(null).build();
    }

    @DeleteMapping("/deploy-ban/{standName}")
    public ResponseEntity<Void> removeBan(@PathVariable String standName) {
        Set<String> keys = redisTemplate.keys("banMessage:" + standName + ":*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        return ResponseEntity.noContent().build();
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }

}
