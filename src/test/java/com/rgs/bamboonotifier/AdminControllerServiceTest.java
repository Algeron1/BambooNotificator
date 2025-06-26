package com.rgs.bamboonotifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.Entity.AnnouncementMessage;
import com.rgs.bamboonotifier.sender.MessageSender;
import com.rgs.bamboonotifier.service.AdminControllerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminControllerServiceTest {

    @Mock
    private MessageSender messageSender;

    @InjectMocks
    private AdminControllerService adminControllerService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    public void testSendMessage() {
        String message = "Hello World";
        adminControllerService.sendTextMessage(message);
        verify(messageSender, times(1)).sendTextMessage(message);
    }

    @Test
    public void testCreateAnnouncement() throws Exception {
        AnnouncementMessage announcement = new AnnouncementMessage();
        announcement.setAuthor("Админ");
        announcement.setText("Тестовое объявление");
        announcement.setWarningLevel("INFO");
        announcement.setFrom(LocalDateTime.now());
        announcement.setTo(LocalDateTime.now().plusHours(24));

        String expectedKeyPrefix = "announcementMessage:";

        String json = "{\"some\":\"json\"}";
        when(objectMapper.writeValueAsString(any())).thenReturn(json);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        adminControllerService.createAnnouncement(announcement);

        assertThat(announcement.getId()).isNotNull();

        verify(objectMapper, times(1)).writeValueAsString(announcement);

        verify(redisTemplate.opsForValue(), times(1)).set(
                startsWith(expectedKeyPrefix),
                eq(json),
                any(Duration.class)
        );
    }
}
