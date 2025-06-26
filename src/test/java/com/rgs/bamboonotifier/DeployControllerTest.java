package com.rgs.bamboonotifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.Entity.DeployBanMessage;
import com.rgs.bamboonotifier.Rest.DeployController;
import com.rgs.bamboonotifier.service.DeployContollerService;
import com.rgs.bamboonotifier.sender.MessageSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeployController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DeployControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeployContollerService deployContollerService;

    @MockitoBean
    private MessageSender messageSender;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getDeployments_sucess() throws Exception {
        when(deployContollerService.getDeploymentInfo()).thenReturn(Collections.emptyList());
        when(deployContollerService.deployBanMessages()).thenReturn(Collections.emptyList());
        when(deployContollerService.getAnnouncementMessageInfo()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.deployments").isArray())
                .andExpect(jsonPath("$.deployBans").isArray())
                .andExpect(jsonPath("$.announcementMessageInfos").isArray());
    }

    @Test
    void getAnnouncements_shouldReturnNoContentIfEmpty() throws Exception {
        when(deployContollerService.getAnnouncementMessages()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/announcements"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createBan_shouldReturnBadRequestIfDateInPast() throws Exception {
        var ban = new DeployBanMessage("Id", "standName", "reason", "author", LocalDateTime.now().minusDays(1), null);
        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/deploy-ban")
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(ban)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeBan_shouldReturnBadRequestIfPinCodeInvalid() throws Exception {
        String id = "some-id";
        var request = Collections.singletonMap("pinCode", "abc");

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete("/deploy-ban/" + id)
                                .contentType("application/json")
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}
