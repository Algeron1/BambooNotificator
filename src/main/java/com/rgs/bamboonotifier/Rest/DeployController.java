package com.rgs.bamboonotifier.Rest;

import com.rgs.bamboonotifier.DTO.AnnouncementMessageInfo;
import com.rgs.bamboonotifier.DTO.DeploymentInfo;
import com.rgs.bamboonotifier.DTO.DeploymentsResponse;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.Entity.DeployBanMessage;
import com.rgs.bamboonotifier.sender.MessageSender;
import com.rgs.bamboonotifier.service.DeployContollerService;
import io.lettuce.core.RedisLoadingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import javax.naming.AuthenticationException;
import java.time.LocalDateTime;
import java.util.*;

@RestController("/")
public class DeployController {

    private final MessageSender messageSender;
    private final DeployContollerService deployContollerService;

    public DeployController(MessageSender messageSender,
                            DeployContollerService deployContollerService) {
        this.messageSender = messageSender;
        this.deployContollerService = deployContollerService;
    }

    @GetMapping("/deployments")
    @ResponseBody
    public ResponseEntity<DeploymentsResponse> getDeployments() {

        List<DeploymentInfo> deployments = deployContollerService.getDeploymentInfo();
        List<DeployBanMessage> activeBans = deployContollerService.deployBanMessages();
        List<AnnouncementMessageInfo> activeAnnouncement = deployContollerService.getAnnouncementMessageInfo();

        DeploymentsResponse response = new DeploymentsResponse();
        response.setDeployments(deployments);
        response.setDeployBans(activeBans);
        response.setAnnouncementMessageInfos(activeAnnouncement);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/deploy-ban")
    public ResponseEntity<String> createBan(@RequestBody DeployBanMessage ban) {
        if (ban.getFrom().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body("Дата начала не может быть в прошлом");
        }
        try {
            deployContollerService.createDeployBanMessage(ban);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        messageSender.sendDeployBanMessage(ban);
        return ResponseEntity.created(null).build();
    }

    @DeleteMapping("/deploy-ban/{id}")
    public ResponseEntity<String> removeBan(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        String pinCode = request.get("pinCode");
        if (pinCode == null || !pinCode.matches("\\d{4}")) {
            return ResponseEntity.badRequest().body("Некорректный пин-код");
        }
        try {
            deployContollerService.deleteDeployBanMessage(pinCode, id);
        } catch (RedisLoadingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok("Бан успешно снят");
    }

    @GetMapping("/announcements")
    @ResponseBody
    public ResponseEntity<Iterable<AnnouncementMessage>> getAnnouncements() {
        List<AnnouncementMessage> announcementMessages = deployContollerService.getAnnouncementMessages();
        if (announcementMessages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(announcementMessages);
    }
}
