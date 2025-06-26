package com.rgs.bamboonotifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.DTO.AnnouncementMessageInfo;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.Rest.AdminController;
import com.rgs.bamboonotifier.service.AdminControllerService;
import io.lettuce.core.RedisException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminControllerService adminControllerService;

    @Autowired
    private ObjectMapper objectMapper;

    private AnnouncementMessage announcement;

    @BeforeEach
    void setUp() {
        announcement = new AnnouncementMessage();
        announcement.setText("Тестовое объявление");
    }

    @Test
    void createAnnouncement_success() throws Exception {
        mockMvc.perform(post("/admin/announcement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(announcement)))
                .andExpect(status().isOk())
                .andExpect(content().string("Объявление создано"));

        verify(adminControllerService).createAnnouncement(any());
        verify(adminControllerService).sendAnnouncementMessage(any());
    }

    @Test
    void createAnnouncement_failure() throws Exception {
        doThrow(new RuntimeException("Ошибка создания")).when(adminControllerService).createAnnouncement(any());

        mockMvc.perform(post("/admin/announcement")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(announcement)))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Ошибка:")));

        verify(adminControllerService).createAnnouncement(any());
    }

    @Test
    void getAnnouncements_success() throws Exception {
        AnnouncementMessageInfo info = new AnnouncementMessageInfo();
        info.setText("Текст");
        when(adminControllerService.getAllAnnouncement()).thenReturn(List.of(info));

        mockMvc.perform(get("/admin/announcements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].text").value("Текст"));

        verify(adminControllerService).getAllAnnouncement();
    }

    @Test
    void sendText_success() throws Exception {
        mockMvc.perform(post("/admin/sendText")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("Тестовое сообщение"))
                .andExpect(status().isOk())
                .andExpect(content().string("Сообщение отправлено"));

        verify(adminControllerService).sendTextMessage("Тестовое сообщение");
    }

    @Test
    void deleteAnnouncement_success() throws Exception {
        mockMvc.perform(delete("/admin/announcement/123"))
                .andExpect(status().isOk())
                .andExpect(content().string("Объявление удалено"));

        verify(adminControllerService).deleteAnnouncement("123");
    }

    @Test
    void deleteAnnouncement_redisError() throws Exception {
        doThrow(new RedisException("Объявление не найдено")).when(adminControllerService).deleteAnnouncement("123");

        mockMvc.perform(delete("/admin/announcement/123"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Объявление не найдено"));

        verify(adminControllerService).deleteAnnouncement("123");
    }

    @Test
    void updateSettings_success() throws Exception {
        Map<String, Boolean> settings = Map.of("setting1", true);

        mockMvc.perform(post("/admin/changesettings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(settings)))
                .andExpect(status().isOk())
                .andExpect(content().string("Настройки обновлены"));

        verify(adminControllerService).updateSettings(settings);
    }

    @Test
    void getCurrentSettings_success() throws Exception {
        Map<String, Boolean> settings = Map.of("setting1", true);
        when(adminControllerService.loadSettings()).thenReturn(settings);

        mockMvc.perform(get("/admin/currentsettings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.setting1").value(true));

        verify(adminControllerService).loadSettings();
    }

    @Test
    void getCurrentSettings_exception() throws Exception {
        when(adminControllerService.loadSettings()).thenThrow(new RuntimeException("Ошибка"));

        mockMvc.perform(get("/admin/currentsettings"))
                .andExpect(status().isOk())
                .andExpect(content().string("{}"));

        verify(adminControllerService).loadSettings();
    }
}
