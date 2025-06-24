package com.rgs.bamboonotifier.Entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;
import java.time.LocalDateTime;

@RedisHash("AnnouncementMessage")
public class AnnouncementMessage implements Serializable {

    @Id
    private String id;
    private String author;
    private LocalDateTime from;
    private LocalDateTime to;
    private String text;
    private String warningLevel;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getWarningLevel() {
        return warningLevel;
    }

    public void setWarningLevel(String warningLevel) {
        this.warningLevel = warningLevel;
    }
}
