package com.rgs.bamboonotifier.sender;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.PachkaResponse;
import com.rgs.bamboonotifier.Entity.DeployBan;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.interfaces.IMessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class PachkaMessageSender extends AbstractMessageSender {

    private final IMessageSender<PachkaResponse> pachkaService;

    @Value("${notification.pachka.enabled}")
    private Boolean pachkaEnabled;

    public PachkaMessageSender(IMessageSender<PachkaResponse> pachkaService) {
        this.pachkaService = pachkaService;
    }

    @Override
    public void send(DeployResult deployResult, String standName, String environmentId, DeployMessage deployMessage) {
        if (!pachkaEnabled) return;

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
    public void sendDeployBanMessage(DeployBan deployBan) {
        if (!pachkaEnabled) return;
        pachkaService.sendMessage(formatDeployBanMessage(deployBan), null);
    }

    @Override
    public void sendtextMessage(String text) {
        if (!pachkaEnabled) return;
        pachkaService.sendMessage(text, null);
    }
}
