package com.rgs.bamboonotifier;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rgs.bamboonotifier.Entity.DeployBanMessage;
import com.rgs.bamboonotifier.service.DeployContollerService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DeployControllerServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DeployContollerService deployContollerService;


    @Test
    void createDeployBanMessage_shouldStoreInRedis() throws Exception {
        DeployBanMessage ban = new DeployBanMessage("Id", "StandName", "Reason", "Author", LocalDateTime.now().minusHours(1), null);
        ban.setTo(LocalDateTime.now().plusHours(1));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"id\":\"abc\"}");

        deployContollerService.createDeployBanMessage(ban);

        assertThat(ban.getId()).isNotNull();

        verify(redisTemplate.opsForValue(), times(1)).set(
                startsWith("banMessage:"),
                eq("{\"id\":\"abc\"}"),
                any(Duration.class)
        );
    }
}
