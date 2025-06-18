package com.rgs.bamboonotifier.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

@RedisHash("DeployBan")
public class DeployBan implements Serializable {

    @Id
    private Long id;
    private String standName;
    private String reason;
    private String author;
    private LocalDateTime from;
    private LocalDateTime to;

    public DeployBan(Long id, String standName, String reason, String author, LocalDateTime from, LocalDateTime to) {
        this.id = id;
        this.standName = standName;
        this.reason = reason;
        this.author = author;
        this.from = from;
        this.to = to;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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
}
