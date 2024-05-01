package com.omegafrog.My.piano.app.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.external.tossPayment.*;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.web.service.admin.option.DisablePostStrategy;
import com.omegafrog.My.piano.app.web.service.admin.option.DisableSheetPostStrategy;
import com.omegafrog.My.piano.app.web.service.admin.option.PostStrategy;
import com.omegafrog.My.piano.app.web.domain.cash.PaymentHistory;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.*;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.web.enums.PaymentType;
import com.omegafrog.My.piano.app.web.service.admin.option.SheetPostStrategy;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class MapperUtil {
    private final ObjectMapper objectMapper;
    private final TossWebHookResultFactory tossWebHookResultFactory;

    public MapperUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.tossWebHookResultFactory = new TossWebHookResultFactoryImpl(objectMapper);
    }

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
                .sheetDto(objectMapper.convertValue(node.get("sheet"), RegisterSheetDto.class))
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

    public List<PostStrategy> parseUpdatePostOption(String body) throws JsonProcessingException {
        List<PostStrategy> strategies = new ArrayList<>();
        JsonNode jsonNode = objectMapper.readTree(body);
        jsonNode.fields().forEachRemaining(entry -> {
            switch (entry.getKey()){
                case DisablePostStrategy.OPTION_NAME ->
                        strategies.add(new DisablePostStrategy(entry.getValue().asBoolean()));
            }
        });
        return strategies;
    }

    public Post parsePostNotiJson(String body, SecurityUser admin) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(body);
        String title = node.get("title").asText();
        String content = node.get("content").asText();

        return Post.builder()
                .title(title)
                .author(admin.getUser())
                .content(content)
                .build();
    }

    public UpdateSheetPostDto parseUpdateSheetPostJson(String dto) throws JsonProcessingException {
        JsonNode node = objectMapper.readTree(dto);
        JsonNode title = node.get("title");
        assert title==null;
        JsonNode content = node.get("content");
        assert content==null;
        JsonNode sheetDto = node.get("sheet");
        assert sheetDto==null;
        JsonNode price = node.get("price");
        assert price==null;
        JsonNode discountRate = node.get("discountRate");
        assert discountRate==null;
        return UpdateSheetPostDto.builder()
                .title(title.asText())
                .content(content.asText())
                .sheet(objectMapper.convertValue(sheetDto, UpdateSheetDto.class))
                .price(price.asInt())
                .discountRate(discountRate.asDouble())
                .build();
    }

    public List<SheetPostStrategy> parseUpdateSheetPostOption(String options) throws JsonProcessingException {
        List<SheetPostStrategy> strategies = new ArrayList<>();
        JsonNode nodes = objectMapper.readTree(options);
        nodes.fields().forEachRemaining(entry->{
            switch (entry.getKey()){
                case (DisableSheetPostStrategy.OPTION_NAME) -> {
                    strategies.add(new DisableSheetPostStrategy(entry.getValue().asBoolean()));
                }
            }
        });
        return strategies;
    }
}
