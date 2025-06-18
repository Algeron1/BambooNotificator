package com.rgs.bamboonotifier.Entity;

import com.rgs.bamboonotifier.DTO.DeployResult;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.time.LocalDateTime;

@RedisHash(value = "DeployMessage", timeToLive = 7 * 24 * 60 * 60)
public class DeployMessage implements Serializable {

    @Id
    private Long deployId;
    private String pachkaMessageId;
    private String telegramMessageId;
    private String message;
    private String author;
    private DeployResult deployResult;
    private LocalDateTime createdAt;

    @Indexed
    private String environmentId;

    public DeployMessage(Long deployId, String pachkaMessageId, String telegramMessageId, String message, String author, DeployResult deployResult, String environmentId) {
        this.deployId = deployId;
        this.pachkaMessageId = pachkaMessageId;
        this.telegramMessageId = telegramMessageId;
        this.message = message;
        this.author = author;
        this.deployResult = deployResult;
        this.environmentId = environmentId;
    }

    public DeployMessage() {
    }

    public DeployResult getDeployResult() {
        return deployResult;
    }

    public void setDeployResult(DeployResult deployResult) {
        this.deployResult = deployResult;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEnvironmentId() {  // Было getEnvorinmenId()
        return environmentId;
    }

    public void setEnvironmentId(String environmentId) {  // Было setEnvorinmenId()
        this.environmentId = environmentId;
    }

    public Long getDeployId() {
        return deployId;
    }

    public void setDeployId(Long deployId) {
        this.deployId = deployId;
    }

    public String getPachkaMessageId() {
        return pachkaMessageId;
    }

    public void setPachkaMessageId(String pachkaMessageId) {
        this.pachkaMessageId = pachkaMessageId;
    }

    public String getTelegramMessageId() {
        return telegramMessageId;
    }

    public void setTelegramMessageId(String telegramMessageId) {
        this.telegramMessageId = telegramMessageId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
