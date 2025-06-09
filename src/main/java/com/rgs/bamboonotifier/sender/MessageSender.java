package com.rgs.bamboonotifier.sender;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.service.PachkaService;
import com.rgs.bamboonotifier.service.TelegramService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {

    private final TelegramService telegramService;
    private final PachkaService pachkaService;

    @Value("${notification.telegram.enabled}")
    private Boolean telegramEnabled;

    @Value("${notification.pachka.enabled}")
    private Boolean pachkaEnabled;

    public MessageSender(TelegramService telegramService, PachkaService pachkaService) {
        this.telegramService = telegramService;
        this.pachkaService = pachkaService;
    }


    public void sendMessage(DeployResult deployResult, String standName) {
        String message = formatMessage(deployResult, standName);
        if (telegramEnabled) {
            telegramService.sendMessage(message);
        }
        if (pachkaEnabled) {
            pachkaService.sendMessage(message);
        }
    }

    private String formatMessage(DeployResult requestDeployResult, String standName) {
        return String.format("""
                        <b>Новый деплой на стенде %s</b>
                        
                        📦 Версия: %s
                        🕓 Время начала: %s
                        🕔 Время завершения: %s
                        👨‍💻 Автор: %s
                        🌿 Бранч: %s
                        📊 Конечный статус: %s
                        📊 Статус деплоя: %s
                        """
                ,
                standName,
                requestDeployResult.getDeploymentVersion().getName(),
                requestDeployResult.getStartedDate().getTime(),
                requestDeployResult.getFinishedDate().getTime(),
                requestDeployResult.getDeploymentVersion().getCreatorDisplayName(),
                requestDeployResult.getDeploymentVersion().getPlanBranchName(),
                requestDeployResult.getDeploymentState(),
                requestDeployResult.getLifeCycleState()
        );
    }
}
