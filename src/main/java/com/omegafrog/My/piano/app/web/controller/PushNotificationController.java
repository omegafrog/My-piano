package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.NotificationApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PushNotificationController {

    private final NotificationApplicationService notificationService;
    private final MapperUtil mapperUtil;

    @PostMapping("/notification/token")
    public JsonAPIResponse getClientToken(
            @Valid @NotNull @RequestBody String clientToken) throws JsonProcessingException {
        notificationService.subscribeUser( mapperUtil.parseNotiClientToken(clientToken));
        return new ApiResponse("구독 성공.");
    }

    @GetMapping("/admin/notification")
    public JsonAPIResponse sendMessage(
            @Valid @NotNull @RequestParam String topic,
            @Valid @NotNull @RequestParam Long userId,
            @Valid @NotNull @RequestParam String message) throws FirebaseMessagingException {
        String id = notificationService.sendMessageTo(topic, message, userId);
        return new ApiResponse("Message 전송 성공.", id);
    }
}
