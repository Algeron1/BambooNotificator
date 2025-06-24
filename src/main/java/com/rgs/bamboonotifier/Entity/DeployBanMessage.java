package com.rgs.bamboonotifier.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@RedisHash("DeployBanMessage")
public class DeployBanMessage implements Serializable {

    @Id
    private String id;
    private String standName;
    private String reason;
    private String author;
    private LocalDateTime from;
    private LocalDateTime to;
    private String pinCode;

    public DeployBanMessage(String id, String standName, String reason, String author, LocalDateTime from, LocalDateTime to) {
        this.id = id;
        this.standName = standName;
        this.reason = reason;
        this.author = author;
        this.from = from;
        this.to = to;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStandName() {
        return standName;
    }

    public void setStandName(String standName) {
        this.standName = standName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public LocalDateTime getFrom() {
        return from;
    }

    public void setFrom(LocalDateTime from) {
        this.from = from;
    }

    public LocalDateTime getTo() {
        return to;
    }

    public void setTo(LocalDateTime to) {
        this.to = to;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }
}
