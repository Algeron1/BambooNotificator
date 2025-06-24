package com.rgs.bamboonotifier.sender;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.TelegramResponse;
import com.rgs.bamboonotifier.Entity.DeployBanMessage;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.interfaces.IMessageSender;
import com.rgs.bamboonotifier.service.NotificationSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TelegramMessageSender extends AbstractMessageSender {

    private final IMessageSender<TelegramResponse> telegramService;
    private final NotificationSettingsService notificationSettingsService;

    public TelegramMessageSender(IMessageSender<TelegramResponse> telegramService,
                                 NotificationSettingsService notificationSettingsService) {
        this.telegramService = telegramService;
        this.notificationSettingsService = notificationSettingsService;
    }

    @Override
    public void send(DeployResult deployResult, String standName, String environmentId, DeployMessage deployMessage) {
        if (!notificationSettingsService.isTelegramEnabled()) return;

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
    public void sendDeployBanMessage(DeployBanMessage deployBanMessage) {
        if (!notificationSettingsService.isTelegramEnabled()) return;
        telegramService.sendMessage(formatDeployBanMessage(deployBanMessage), null);
    }

    @Override
    public void sendtextMessage(String text) {
        if (!notificationSettingsService.isTelegramEnabled()) return;
        telegramService.sendMessage(text, null);
    }
}
