package com.rgs.bamboonotifier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telegram.api")
public class TelegramProperties {
    private String url;
    private String chatId;

    public void setUrl(String url) {
        this.url = url;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getUrl() {
        return url;
    }
}
