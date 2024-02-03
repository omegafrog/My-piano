package com.omegafrog.My.piano.app.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.external.tossPayment.Payment;
import com.omegafrog.My.piano.app.external.tossPayment.TossError;
import com.omegafrog.My.piano.app.external.tossPayment.TossWebHookResult;
import com.omegafrog.My.piano.app.external.tossPayment.TossWebHookResultFactory;
import com.omegafrog.My.piano.app.web.domain.cash.PaymentHistory;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.ChangeUserDto;
import com.omegafrog.My.piano.app.web.dto.RegisterSheetDto;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.web.enums.PaymentType;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class MapperUtil {
    private final ObjectMapper objectMapper;

    private final TossWebHookResultFactory tossWebHookResultFactory;

    public PaymentHistory parsePaymentHistory(String result) throws JsonProcessingException {
            JsonNode node = objectMapper.readTree(result);
            return new PaymentHistory(
                    node.get("paymentKey").asText(),
                    node.get("orderId").asText(),
                    PaymentType.valueOf(node.get("paymentType").asText()),
                    node.get("orderName").asText(),
                    node.get("totalAmount").asInt(),
                    LocalDateTime.parse(node.get("requestedAt").asText()),
                    OrderStatus.valueOf(node.get("status").asText()));
}

    public RegisterSheetPostDto parseRegisterSheetPostInfo(String registerSheetInfo, User loggedInUser) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(registerSheetInfo);

        return RegisterSheetPostDto.builder()
                .title(node.get("title").asText())
                .content(node.get("content").asText())
                .price(node.get("price").asInt())
                .discountRate(node.get("discountRate").asDouble())
                .artistId(loggedInUser.getId())
                .sheetDto(objectMapper.convertValue(node.get("sheetDto"), RegisterSheetDto.class))
                .build();
    }

    public RegisterUserDto parseRegisterUserInfo(String registerInfo) throws JsonProcessingException {
        JsonNode registerNodeInfo = objectMapper.readTree(registerInfo);
        String username = registerNodeInfo.get("username").asText();
        String password = registerNodeInfo.get("password").asText();
        String name = registerNodeInfo.get("name").asText();
        String email = registerNodeInfo.get("email").asText();
        String phoneNum = registerNodeInfo.get("phoneNum").asText();
        String loginMethod = registerNodeInfo.get("loginMethod").asText();
        String profileSrc = registerNodeInfo.get("profileSrc").asText();
        return RegisterUserDto.builder()
                .username(username)
                .password(password)
                .name(name)
                .email(email)
                .phoneNum(phoneNum)
                .loginMethod(LoginMethod.valueOf(loginMethod))
                .profileSrc(profileSrc)
                .build();
    }

    public ChangeUserDto parseUpdateUserInfo(String dto) throws JsonProcessingException {
        JsonNode registerNodeInfo = objectMapper.readTree(dto);
        String currentPassword = registerNodeInfo.get("currentPassword").asText();
        String changedPassword = registerNodeInfo.get("changedPassword").asText();
        String name = registerNodeInfo.get("name").asText();
        String phoneNum = registerNodeInfo.get("phoneNum").asText();
        String profileSrc = registerNodeInfo.get("profileSrc").asText();

        return ChangeUserDto.builder()
                .name(name)
                .phoneNum(phoneNum)
                .currentPassword(currentPassword)
                .changedPassword(changedPassword)
                .profileSrc(profileSrc)
                .build();
    }

    public Payment parsePayment(String json) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(json);
        return new Payment(jsonNode.get("paymentKey").asText(),
                jsonNode.get("type").asText(),
                jsonNode.get("orderId").asText(),
                jsonNode.get("orderName").asText(),
                jsonNode.get("mId").asText(),
                jsonNode.get("currency").asText(),
                jsonNode.get("method").asText(),
                jsonNode.get("totalAmount").asLong(),
                jsonNode.get("status").asText());
    }

    public TossError parseTossError(String json){
        return objectMapper.convertValue(json, TossError.class);
    }

    public TossWebHookResult parseTossWebhook(String json) throws JsonProcessingException {
        return tossWebHookResultFactory.parse(json);
    }
}
