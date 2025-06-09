package com.rgs.bamboonotifier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "bamboo")
public class BambooProperties {

    private Map<String, String> deploymentIds;

    public Map<String, String> getDeploymentIds() {
        return deploymentIds;
    }

    public void setDeploymentIds(Map<String, String> deploymentIds) {
        this.deploymentIds = deploymentIds;
    }
}
