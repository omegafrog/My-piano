package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.service.NotificationApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PushNotificationController {

    private final NotificationApplicationService notificationService;
    private final MapperUtil mapperUtil;

    @PostMapping("/notification/token")
    public JsonAPIResponse getClientToken(@RequestBody String clientToken) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        notificationService.subscribeUser( mapperUtil.parseNotiClientToken(clientToken),
                loggedInUser.getId());
        return new APISuccessResponse("구독 성공.");
    }

    @GetMapping("/admin/notification")
    public JsonAPIResponse sendMessage(@RequestParam String topic, @RequestParam Long userId, @RequestParam String message) throws FirebaseMessagingException, JsonProcessingException {
        String id = notificationService.sendMessageTo(topic, message, userId);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("messageId", id);
        return new APISuccessResponse("Message 전송 성공.", data);
    }
}
