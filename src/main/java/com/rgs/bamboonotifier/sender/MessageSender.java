package com.rgs.bamboonotifier.sender;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.Repository.DeployMessageRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {

    private final TelegramMessageSender telegramMessageSender;
    private final PachkaMessageSender pachkaMessageSender;
    private final DeployMessageRepository deployMessageRepository;

    public MessageSender(TelegramMessageSender telegramMessageSender,
                         PachkaMessageSender pachkaMessageSender,
                         DeployMessageRepository deployMessageRepository) {
        this.telegramMessageSender = telegramMessageSender;
        this.pachkaMessageSender = pachkaMessageSender;
        this.deployMessageRepository = deployMessageRepository;
    }

    public void sendMessage(DeployResult deployResult, String standName, String environmentId) {
        DeployMessage deployMessage = deployMessageRepository.findByDeployId(deployResult.getDeploymentVersion().getId());
        if (deployMessage == null) {
            deployMessage = new DeployMessage();
            deployMessage.setDeployId(deployResult.getDeploymentVersion().getId());
        }
        deployMessage.setEnvironmentId(environmentId);
        deployMessage.setDeployResult(deployResult);

        telegramMessageSender.send(deployResult, standName, environmentId, deployMessage);
        pachkaMessageSender.send(deployResult, standName, environmentId, deployMessage);
    }
}
