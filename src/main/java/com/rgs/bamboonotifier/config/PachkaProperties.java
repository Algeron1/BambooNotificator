package com.rgs.bamboonotifier.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "pachka.api")
public class PachkaProperties {
    private String url;
    private String token;
    private String entity_id;

    public void setEntity_id(String entity_id) {
        this.entity_id = entity_id;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public String getUrl() {
        return url;
    }

    public String getEntity_id() {
        return entity_id;
    }
}
