
package com.rgs.bamboonotifier.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.rgs.bamboonotifier.DTO.BambooResult;
import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.DeployResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

@Service
public class BambooService {

    private static final Logger logger = LoggerFactory.getLogger(BambooService.class);

    private final String bambooApiUrl;
    private final String username;
    private final String password;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public BambooService(RestTemplate restTemplate,
                         ObjectMapper objectMapper,
                         @Value("${bamboo.api.url}") String bambooApiUrl,
                         @Value("${bamboo.api.user}") String username,
                         @Value("${bamboo.api.password}") String password) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.bambooApiUrl = bambooApiUrl;
        this.username = username;
        this.password = password;
    }

    public DeployResult getDeploymentStatus(String environmentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = bambooApiUrl + "/deploy/environment/" + environmentId + "/results.json?expand=results.result.deploymentVersion";
        ResponseEntity<DeployResults> response = restTemplate.exchange(url, HttpMethod.GET, entity, DeployResults.class);
        DeployResults body = response.getBody();
        if (body == null) {
            return null;
        }
        return body.getResults().getFirst();
    }

    public Optional<BambooResult> getBuildResult(String planResultKey) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = bambooApiUrl + "/result/" + planResultKey + ".json?expand=testResults";
            ResponseEntity<BambooResult> response = restTemplate.exchange(url, HttpMethod.GET, entity, BambooResult.class);
            BambooResult body = response.getBody();
            if (body != null) {
                logger.debug("Билд {}: state={}, passed={}, failed={}", planResultKey, body.getState(), body.getSuccessfulTestCount(), body.getFailedTestCount());
            }
            return Optional.ofNullable(body);
        } catch (Exception e) {
            logger.warn("Не удалось получить результат билда {}: {}", planResultKey, e.getMessage());
        }
        return Optional.empty();
    }

    public String getPlanResultKeyFromDeployResult(long deployResultId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(username, password);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            String url = bambooApiUrl + "/deploy/result/" + deployResultId;
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getBody() == null) return null;

            JsonNode root = objectMapper.readTree(response.getBody());

            JsonNode items = root.path("deploymentVersion").path("items");
            if (items.isArray()) {
                for (JsonNode item : items) {
                    JsonNode artKey = item.path("artifact").path("planResultKey").path("key");
                    if (artKey.isTextual() && !artKey.asText().startsWith("DELETED_"))
                        return artKey.asText();
                }
                for (JsonNode item : items) {
                    JsonNode itemKey = item.path("planResultKey").path("key");
                    if (itemKey.isTextual() && !itemKey.asText().startsWith("DELETED_"))
                        return itemKey.asText();
                }
            }

            JsonNode reason = root.path("reasonSummary");
            if (reason.isTextual()) {
                Matcher m = Pattern.compile("browse/([A-Z]+-[A-Z0-9]+-\\d+)").matcher(reason.asText());
                if (m.find()) return m.group(1);
            }

            logger.debug("planResultKey не найден в /deploy/result/{}, тело: {}", deployResultId, response.getBody());
        } catch (Exception e) {
            logger.warn("Ошибка при запросе /deploy/result/{}: {}", deployResultId, e.getMessage());
        }
        return null;
    }
}
