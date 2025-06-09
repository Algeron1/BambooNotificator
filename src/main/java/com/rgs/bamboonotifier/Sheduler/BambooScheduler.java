package com.rgs.bamboonotifier.Sheduler;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.config.BambooProperties;
import com.rgs.bamboonotifier.sender.MessageSender;
import com.rgs.bamboonotifier.service.BambooService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BambooScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BambooScheduler.class);

    private final BambooService bambooService;
    private final BambooProperties bambooProperties;
    private final MessageSender messageSender;


    private final Map<String, DeployResult> lastDeploys = new HashMap<>();

    public BambooScheduler(BambooService bambooService, MessageSender sender, BambooProperties bambooProperties) {
        this.bambooService = bambooService;
        this.messageSender = sender;
        this.bambooProperties = bambooProperties;
    }

    @Scheduled(fixedRate = 30000)
    public void checkDeploymentStatuses() {
        for (Map.Entry<String, String> entry : bambooProperties.getDeploymentIds().entrySet()) {
            String deploymentId = entry.getKey();
            String standName = entry.getValue();

            try {
                DeployResult status = bambooService.getDeploymentStatus(deploymentId);
                if (status == null) {
                    logger.warn("Не удалось получить статус для стенда {}", standName);
                    continue;
                }

                DeployResult lastDeploy = lastDeploys.get(deploymentId);

                if (lastDeploy == null || !lastDeploy.equals(status)) {
                    messageSender.sendMessage(status, standName);
                    logger.info("Отправлено уведомление по стенду {}", standName);
                    lastDeploys.put(deploymentId, status);
                }
            } catch (Exception e) {
                logger.error("Ошибка при проверке стенда {}: {}", standName, e.getMessage());
            }
        }
    }
}
