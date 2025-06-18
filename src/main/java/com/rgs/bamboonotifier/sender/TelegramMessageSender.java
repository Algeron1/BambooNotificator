package com.rgs.bamboonotifier.sender;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.TelegramResponse;
import com.rgs.bamboonotifier.Entity.DeployBan;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.interfaces.IMessageSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TelegramMessageSender extends AbstractMessageSender {

    private final IMessageSender<TelegramResponse> telegramService;

    @Value("${notification.telegram.enabled}")
    private Boolean telegramEnabled;

    public TelegramMessageSender(IMessageSender<TelegramResponse> telegramService) {
        this.telegramService = telegramService;
    }

    @Override
    public void send(DeployResult deployResult, String standName, String environmentId, DeployMessage deployMessage) {
        if (!telegramEnabled) return;

        String message = formatMessage(deployResult, standName);
        ResponseEntity<TelegramResponse> response;

        if (deployMessage.getTelegramMessageId() != null && deployResult.getId() == deployMessage.getDeployResult().getId()) {
            response = telegramService.sendMessage(message, deployMessage.getTelegramMessageId());
        } else {
            response = telegramService.sendMessage(message, null);
        }

        TelegramResponse telegramResponse = response.getBody();
        if (telegramResponse != null && telegramResponse.getResult() != null) {
            deployMessage.setTelegramMessageId(String.valueOf(telegramResponse.getResult().getMessageId()));
        }

        saveDeployMessage(deployMessage);
    }

    @Override
    public void sendDeployBanMessage(DeployBan deployBan) {
        if (!telegramEnabled) return;
        telegramService.sendMessage(formatDeployBanMessage(deployBan), null);
    }
}
