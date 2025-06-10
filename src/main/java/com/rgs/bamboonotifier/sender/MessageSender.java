package com.rgs.bamboonotifier.sender;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.PachkaResponse;
import com.rgs.bamboonotifier.DTO.TelegramResponse;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.Repository.DeployMessageRepository;
import com.rgs.bamboonotifier.constants.ApplicationConstants;
import com.rgs.bamboonotifier.service.PachkaService;
import com.rgs.bamboonotifier.service.TelegramService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class MessageSender {

    private final TelegramService telegramService;
    private final PachkaService pachkaService;
    private final DeployMessageRepository deployMessageRepository;

    @Value("${notification.telegram.enabled}")
    private Boolean telegramEnabled;

    @Value("${notification.pachka.enabled}")
    private Boolean pachkaEnabled;

    public MessageSender(TelegramService telegramService, PachkaService pachkaService, DeployMessageRepository deployMessageRepository) {
        this.telegramService = telegramService;
        this.pachkaService = pachkaService;
        this.deployMessageRepository = deployMessageRepository;
    }

    public void sendMessage(DeployResult deployResult, String standName, String environmentId) {
        String message = formatMessage(deployResult, standName);
        DeployMessage deployMessage = getDeployMessage(environmentId);

        if (deployMessage == null || deployMessage.getDeployResult() != deployResult) {
            deployMessage = new DeployMessage();
        }

        deployMessage.setEnvironmentId(environmentId);
        deployMessage.setDeployResult(deployResult);
        deployMessage.setDeployId(deployResult.getDeploymentVersion().getId());

        if (telegramEnabled) {
            ResponseEntity<TelegramResponse> response;
            if (deployMessage.getTelegramMessageId() != null && deployResult.getDeploymentVersion().getId() == deployMessage.getDeployId()) {
                response = telegramService.sendMessage(message, deployMessage.getTelegramMessageId());
            } else {
                response = telegramService.sendMessage(message, null);
            }
            TelegramResponse telegramResponse = response.getBody();
            deployMessage.setTelegramMessageId(String.valueOf(telegramResponse.getResult().getMessageId()));
        }
        if (pachkaEnabled) {
            ResponseEntity<PachkaResponse> response;
            if (deployMessage.getPachkaMessageId() != null && deployResult.getDeploymentVersion().getId() == deployMessage.getDeployId()) {
                response = pachkaService.sendMessage(message, deployMessage.getPachkaMessageId());
            } else {
                response = pachkaService.sendMessage(message, null);
            }
            PachkaResponse pachkaResponse = response.getBody();
            deployMessage.setPachkaMessageId(String.valueOf(pachkaResponse.getData().getId()));
        }
        saveDeployMessage(deployMessage);
    }

    public void saveDeployMessage(DeployMessage message) {
        deployMessageRepository.save(message);
    }

    public DeployMessage getDeployMessage(String deployId) {
        return deployMessageRepository.findById(deployId).orElse(null);
    }

    private String formatMessage(DeployResult deployResult, String standName) {
        return String.format(ApplicationConstants.NEW_DEPLOY_MESSAGE_TEMPLATE,
                standName,
                deployResult.getDeploymentVersion().getName(),
                formatDate(deployResult.getStartedDate()),
                formatDate(deployResult.getFinishedDate()),
                deployResult.getDeploymentVersion().getCreatorDisplayName(),
                deployResult.getDeploymentVersion().getPlanBranchName(),
                deployResult.getDeploymentState(),
                deployResult.getLifeCycleState()
        );
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }
}
