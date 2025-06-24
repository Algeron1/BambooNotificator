package com.rgs.bamboonotifier.DTO;

import com.rgs.bamboonotifier.Entity.DeployBanMessage;

import java.util.List;

public class DeploymentsResponse {
    private List<DeploymentInfo> deployments;
    private List<DeployBanMessage> deployBanMessages;
    private List<AnnouncementMessageInfo> announcementMessageInfos;

    public List<DeploymentInfo> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<DeploymentInfo> deployments) {
        this.deployments = deployments;
    }

    public List<DeployBanMessage> getDeployBans() {
        return deployBanMessages;
    }

    public void setDeployBans(List<DeployBanMessage> deployBanMessages) {
        this.deployBanMessages = deployBanMessages;
    }

    public List<AnnouncementMessageInfo> getAnnouncementMessageInfos() {
        return announcementMessageInfos;
    }

    public void setAnnouncementMessageInfos(List<AnnouncementMessageInfo> announcementMessageInfos) {
        this.announcementMessageInfos = announcementMessageInfos;
    }
}
