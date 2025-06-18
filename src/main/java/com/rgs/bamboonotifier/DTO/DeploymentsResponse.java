package com.rgs.bamboonotifier.DTO;

import com.rgs.bamboonotifier.Entity.DeployBan;

import java.util.List;

public class DeploymentsResponse {
    private List<DeploymentInfo> deployments;
    private List<DeployBan> deployBans;

    public List<DeploymentInfo> getDeployments() {
        return deployments;
    }

    public void setDeployments(List<DeploymentInfo> deployments) {
        this.deployments = deployments;
    }

    public List<DeployBan> getDeployBans() {
        return deployBans;
    }

    public void setDeployBans(List<DeployBan> deployBans) {
        this.deployBans = deployBans;
    }
}
