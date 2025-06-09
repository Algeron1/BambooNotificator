
package com.rgs.bamboonotifier.service;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.DeployResults;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Service
public class BambooService {

    @Value("${bamboo.api.url}")
    private String bambooApiUrl;

    @Value("${bamboo.api.user}")
    private String username;

    @Value("${bamboo.api.password}")
    private String password;

    private final RestTemplate restTemplate = new RestTemplate();

    public DeployResult getDeploymentStatus(String environmentId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(username, password);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = bambooApiUrl + "/deploy/environment/" + environmentId + "/results.json";
        ResponseEntity<DeployResults> response = restTemplate.exchange(url, HttpMethod.GET, entity, DeployResults.class);
        assert response.getBody() != null;
        return response.getBody().getResults().getFirst();
    }
}
