package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.service.NotificationApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PushNotificationController {

    private final NotificationApplicationService notificationService;
    private final MapperUtil mapperUtil;

    @PostMapping("/notification/token")
    public JsonAPISuccessResponse getClientToken(
            @Valid @NotNull @RequestBody String clientToken) throws JsonProcessingException {
        notificationService.subscribeUser( mapperUtil.parseNotiClientToken(clientToken));
        return new ApiSuccessResponse("구독 성공.");
    }

    @GetMapping("/admin/notification")
    public JsonAPISuccessResponse sendMessage(
            @Valid @NotNull @RequestParam String topic,
            @Valid @NotNull @RequestParam Long userId,
            @Valid @NotNull @RequestParam String message) throws FirebaseMessagingException {
        String id = notificationService.sendMessageTo(topic, message, userId);
        return new ApiSuccessResponse("Message 전송 성공.", id);
    }
}
