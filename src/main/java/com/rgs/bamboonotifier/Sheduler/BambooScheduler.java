package com.rgs.bamboonotifier.Sheduler;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.Repository.DeployMessageRepository;
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
    private final DeployMessageRepository deployMessageRepository;

    public BambooScheduler(BambooService bambooService, MessageSender sender, BambooProperties bambooProperties, DeployMessageRepository deployMessageRepository) {
        this.bambooService = bambooService;
        this.messageSender = sender;
        this.bambooProperties = bambooProperties;
        this.deployMessageRepository = deployMessageRepository;
    }

    @Scheduled(fixedRate = 30000)
    public void checkDeploymentStatuses() {
        for (Map.Entry<String, String> entry : bambooProperties.getDeploymentIds().entrySet()) {

            String environmentId = entry.getKey();
            String standName = entry.getValue();

            try {
                DeployResult status = bambooService.getDeploymentStatus(environmentId);
                if (status == null) {
                    logger.warn("Не удалось получить статус для стенда {}", standName);
                    continue;
                }

                DeployMessage lastMessage = getDeployMessage(status.getDeploymentVersion().getId());
                DeployResult lastDeploy = null;

                if (lastMessage != null && lastMessage.getDeployResult() != null) {
                    lastDeploy = lastMessage.getDeployResult();
                }
                if (lastDeploy == null || !lastDeploy.getDeploymentState().equalsIgnoreCase(status.getDeploymentState())
                        && lastDeploy.getId() == status.getId()) {
                    messageSender.sendMessage(status, standName, environmentId);
                    logger.info("Отправлено уведомление по стенду {}", standName);
                }
            } catch (Exception e) {
                logger.error("Ошибка при проверке стенда {}: {}", standName, e.getMessage());
            }
        }
    }

    public DeployMessage getDeployMessage(Long deploymentId) {
        return deployMessageRepository.findByDeployId(deploymentId);
    }
}
