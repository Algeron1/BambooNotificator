package com.rgs.bamboonotifier.Rest;

import com.rgs.bamboonotifier.DTO.DeploymentInfo;
import com.rgs.bamboonotifier.Entity.DeployMessage;
import com.rgs.bamboonotifier.Repository.DeployMessageRepository;
import com.rgs.bamboonotifier.config.BambooProperties;
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
    private final DeployMessageRepository deployMessageRepository;

    public FrontController(BambooProperties bambooProperties, DeployMessageRepository deployMessageRepository) {
        this.bambooProperties = bambooProperties;
        this.deployMessageRepository = deployMessageRepository;
    }

    @GetMapping("/deployments")
    @ResponseBody
    public ResponseEntity<List<DeploymentInfo>> getDeployments() {
        List<DeploymentInfo> deployments = new ArrayList<>();

        for (Map.Entry<String, String> entry : bambooProperties.getDeploymentIds().entrySet()) {
            String environmentId = entry.getKey();
            String standName = entry.getValue();

            DeployMessage deployMessage = deployMessageRepository.findByEnvironmentId(environmentId);
            if (deployMessage != null) {
                DeploymentInfo deploymentInfo = new DeploymentInfo();
                deploymentInfo.setEnvironmentId(standName);
                deploymentInfo.setDeployVersion(deployMessage.getDeployResult().getDeploymentVersion().getName());
                deploymentInfo.setStatus(deployMessage.getDeployResult().getDeploymentState());
                deploymentInfo.setStartedDate(formatDate(deployMessage.getDeployResult().getStartedDate()));
                deploymentInfo.setFinishedDate(formatDate(deployMessage.getDeployResult().getFinishedDate()));
                deploymentInfo.setAuthor(deployMessage.getDeployResult().getDeploymentVersion().getCreatorDisplayName());
                deployments.add(deploymentInfo);
            }
        }

        return ResponseEntity.ok(deployments);
    }

    protected String formatDate(Date date) {
        return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(date);
    }
}
