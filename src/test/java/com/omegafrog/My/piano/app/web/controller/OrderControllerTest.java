package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.Cleanup;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TestUtil testUtil;
    private TestUtil.TokenResponse user1Tokens;
    private TestUtil.TokenResponse user2Tokens;
    private TestUtil.TokenResponse buyerTokens;
    @Autowired
    private SecurityUserRepository securityUserRepository;

    @Autowired
    private Cleanup cleanup;


    @BeforeEach
    void setUp() throws Exception {
        RegisterUserDto user1 = TestUtil.user1;
        testUtil.register(mockMvc, user1);
        user1Tokens = testUtil.login(mockMvc, user1.getUsername(), user1.getPassword());

        RegisterUserDto buyer = TestUtil.user3;
        testUtil.register(mockMvc, buyer);
        buyerTokens = testUtil.login(mockMvc, buyer.getUsername(), buyer.getPassword());

        testUtil.chargeCash(mockMvc, 20000, buyerTokens);

        RegisterUserDto user2 = TestUtil.user2;
        testUtil.register(mockMvc, user2);

        SecurityUser securityUser = securityUserRepository.findByUsername(user2.getUsername())
                .orElseThrow(() -> new EntityNotFoundException());
        securityUser.changeRole(Role.CREATOR);
        securityUserRepository.save(securityUser);
        user2Tokens = testUtil.login(mockMvc, user2.getUsername(), user2.getPassword());
    }

    @AfterEach
    void setCleanup() {
        cleanup.cleanUp();
    }

    private String baseUrl = "/api/v1/order";

    @Test
    void orderSheet() throws Exception {
        SheetPostDto savedSheetPost =
                testUtil.writeSheetPost(mockMvc, user1Tokens, TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1));
        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .build();

        String data = objectMapper.writeValueAsString(orderDto);
        mockMvc.perform(post(baseUrl + "/sheet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .header(HttpHeaders.AUTHORIZATION, buyerTokens.getAccessToken())
                        .cookie(buyerTokens.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print());

        List<SheetPostDto> purchasedSheetPosts = testUtil.getPurchasedSheetPosts(mockMvc, buyerTokens);

        Assertions.assertThat(purchasedSheetPosts).isNotEmpty();
        Assertions.assertThat(purchasedSheetPosts).contains(savedSheetPost);
    }


    @Test
    void orderLesson() throws Exception {
        SheetPostDto savedSheetPost = testUtil.writeSheetPost(mockMvc, user1Tokens, TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1));
        LessonDto savedLesson = testUtil.writeLesson(mockMvc, user2Tokens, TestUtil.registerLessonDto(savedSheetPost.getId()));
        OrderRegisterDto order2 = OrderRegisterDto.builder()
                .itemId(savedLesson.getId())
                .build();

        mockMvc.perform(post(baseUrl + "/lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order2))
                        .header(HttpHeaders.AUTHORIZATION, buyerTokens.getAccessToken())
                        .cookie(buyerTokens.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print());
        List<LessonDto> purchasedLessons = testUtil.getPurchasedLessons(mockMvc, buyerTokens);

        Assertions.assertThat(purchasedLessons).isNotEmpty();
        Assertions.assertThat(purchasedLessons).extracting("id").contains(savedLesson.getId());
    }

    @Test
    void cancelOrderTest() throws Exception {
        SheetPostDto savedSheetPost = testUtil.writeSheetPost(mockMvc, user1Tokens, TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1));
        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .build();
        String data = objectMapper.writeValueAsString(orderDto);
        String response = mockMvc.perform(post(baseUrl + "/sheet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .header(HttpHeaders.AUTHORIZATION, buyerTokens.getAccessToken())
                        .cookie(buyerTokens.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("data").get("id").asLong();

        mockMvc.perform(delete(baseUrl + "/" + id)
                        .header(HttpHeaders.AUTHORIZATION, buyerTokens.getAccessToken())
                        .cookie(buyerTokens.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print());

        mockMvc.perform(get("/api/v1/user/purchasedSheets")
                        .header(HttpHeaders.AUTHORIZATION, buyerTokens.getAccessToken())
                        .cookie(buyerTokens.getRefreshToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andExpect(jsonPath("$.data").isEmpty());

    }

    @Test
    void getAllOrdersTest() throws Exception {

        SheetPostDto savedSheetPost = testUtil.writeSheetPost(mockMvc, user1Tokens, TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1));
        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .build();
        String data = objectMapper.writeValueAsString(orderDto);
        mockMvc.perform(post(baseUrl + "/sheet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .header(HttpHeaders.AUTHORIZATION, buyerTokens.getAccessToken())
                        .cookie(buyerTokens.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print());


        String result = mockMvc.perform(get(baseUrl)
                        .header(HttpHeaders.AUTHORIZATION, buyerTokens.getAccessToken())
                        .cookie(buyerTokens.getRefreshToken())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(objectMapper.readTree(result).get("data")).isNotEmpty();
    }

    @Test
    void createOrderValidationFailTest() throws Exception {
        SheetPostDto savedSheetPost = testUtil.writeSheetPost(mockMvc, user1Tokens, TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1));
        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .build();
        String data = objectMapper.writeValueAsString(orderDto);
        mockMvc.perform(post(baseUrl + "/sheet")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andDo(print());
    }
}