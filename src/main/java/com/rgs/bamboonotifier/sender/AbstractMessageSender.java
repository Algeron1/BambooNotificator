package com.rgs.bamboonotifier.sender;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.Entity.DeployBan;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.Repository.DeployMessageRepository;
import com.rgs.bamboonotifier.constants.ApplicationConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public abstract class AbstractMessageSender<T> {

    @Autowired
    protected DeployMessageRepository deployMessageRepository;

    public abstract void send(DeployResult deployResult, String standName, String environmentId, DeployMessage deployMessage);

    public abstract void sendDeployBanMessage(DeployBan deployBan);

    public abstract void sendtextMessage(String text);

    protected String formatMessage(DeployResult deployResult, String standName) {
        return String.format(getTemplate(deployResult),
                standName,
                deployResult.getDeploymentVersion().getName(),
                deployResult.getStartedDate() == null ? null : formatDate(deployResult.getStartedDate()),
                deployResult.getFinishedDate() == null ? null : formatDate(deployResult.getFinishedDate()),
                deployResult.getDeploymentVersion().getCreatorDisplayName() == null ? "Автодеплой" : deployResult.getDeploymentVersion().getCreatorDisplayName(),
                deployResult.getDeploymentVersion().getPlanBranchName(),
                deployResult.getDeploymentState(),
                deployResult.getLifeCycleState()
        );
    }

    protected String formatDeployBanMessage(DeployBan deployBan) {
        return String.format(ApplicationConstants.DEPLOY_BAN_MESSAGE_TEMPLATE,
                deployBan.getStandName(),
                deployBan.getReason(),
                formatDate(deployBan.getFrom()),
                formatDate(deployBan.getTo()),
                deployBan.getAuthor()
        );
    }

    protected String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    protected String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }

    protected String getTemplate(DeployResult result) {
        if (result.getDeploymentState().equalsIgnoreCase(ApplicationConstants.SUCCESS_STATUS)) {
            return ApplicationConstants.SUCCESS_DEPLOY_MESSAGE_TEMPLATE;
        }
        if (result.getLifeCycleState().equalsIgnoreCase(ApplicationConstants.IN_PROGRESS_STATUS)
                && result.getDeploymentState().equalsIgnoreCase(ApplicationConstants.UNKNOWN_STATUS)) {
            return ApplicationConstants.NEW_DEPLOY_MESSAGE_TEMPLATE;
        }
        return ApplicationConstants.ERROR_DEPLOY_MESSAGE_TEMPLATE;
    }

    protected void saveDeployMessage(DeployMessage message) {
        deployMessageRepository.save(message);
    }
}

