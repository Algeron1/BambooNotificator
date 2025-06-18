package com.rgs.bamboonotifier.Rest;

import com.rgs.bamboonotifier.DTO.DeployResult;
import com.rgs.bamboonotifier.DTO.DeploymentInfo;
import com.rgs.bamboonotifier.config.BambooProperties;
import com.rgs.bamboonotifier.service.BambooService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
class FrontController {
    private final BambooProperties bambooProperties;
    private final BambooService bambooService;

    public FrontController(BambooProperties bambooProperties, BambooService bambooService) {
        this.bambooProperties = bambooProperties;
        this.bambooService = bambooService;
    }

    @GetMapping("/deployments")
    @ResponseBody
    public ResponseEntity<List<DeploymentInfo>> getDeployments() {
        List<DeploymentInfo> deployments = new ArrayList<>();

        for (Map.Entry<String, String> entry : bambooProperties.getDeploymentIds().entrySet()) {
            String environmentId = entry.getKey();
            String standName = entry.getValue();

           DeployResult result = bambooService.getDeploymentStatus(environmentId);
            if (result != null) {
                DeploymentInfo deploymentInfo = new DeploymentInfo();
                deploymentInfo.setEnvironmentId(standName);
                deploymentInfo.setDeployVersion(result.getDeploymentVersion().getName());
                deploymentInfo.setStatus(result.getDeploymentState());
                deploymentInfo.setStartedDate(result.getStartedDate() == null ? null : formatDate(result.getStartedDate()));
                deploymentInfo.setFinishedDate(result.getFinishedDate() == null ? null : formatDate(result.getFinishedDate()));
                deploymentInfo.setAuthor(result.getDeploymentVersion().getCreatorDisplayName() == null ? "Автодеплой" : result.getDeploymentVersion().getCreatorDisplayName());
                deployments.add(deploymentInfo);
            }
        }

        return ResponseEntity.ok(deployments);
    }

    protected String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }
}
