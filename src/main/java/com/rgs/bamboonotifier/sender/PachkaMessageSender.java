package com.rgs.bamboonotifier.sender;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.PachkaResponse;
import com.rgs.bamboonotifier.Entity.DeployBanMessage;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.interfaces.IMessageSender;
import com.rgs.bamboonotifier.service.NotificationSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PachkaMessageSender extends AbstractMessageSender {

    private final IMessageSender<PachkaResponse> pachkaService;
    private final NotificationSettingsService notificationSettingsService;

    public PachkaMessageSender(IMessageSender<PachkaResponse> pachkaService,
                               NotificationSettingsService notificationSettingsService) {
        this.pachkaService = pachkaService;
        this.notificationSettingsService = notificationSettingsService;
    }

    @Override
    public void send(DeployResult deployResult, String standName, String environmentId, DeployMessage deployMessage) {
        if (!notificationSettingsService.isPachkaEnabled()) return;

        String message = formatMessage(deployResult, standName);
        ResponseEntity<PachkaResponse> response;

        if (deployMessage.getPachkaMessageId() != null && deployResult.getId() == deployMessage.getDeployResult().getId()) {
            response = pachkaService.sendMessage(message, deployMessage.getPachkaMessageId());
        } else {
            response = pachkaService.sendMessage(message, null);
        }

        PachkaResponse pachkaResponse = response.getBody();
        deployMessage.setPachkaMessageId(String.valueOf(pachkaResponse.getData().getId()));

        saveDeployMessage(deployMessage);
    }

    @Override
    public void sendDeployBanMessage(DeployBanMessage deployBanMessage) {
        if (!notificationSettingsService.isPachkaEnabled()) return;
        pachkaService.sendMessage(formatDeployBanMessage(deployBanMessage), null);
    }

    @Override
    public void sendtextMessage(String text) {
        if (!notificationSettingsService.isPachkaEnabled()) return;
        pachkaService.sendMessage(text, null);
    }
}
