package com.omegafrog.My.piano.app.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.dto.WritePostDto;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import jakarta.servlet.http.Cookie;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class PostControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CommonUserService commonUserService;

    @Autowired
    private ObjectMapper objectMapper;

    private static RegisterUserDto user1;
    private static RegisterUserDto user2;


    @BeforeAll
     static void setUsers(){
          user1 = RegisterUserDto.builder()
                .name("user1")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .isAuthorized(false)
                        .build())
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                 .username("user1")
                 .password("password")
                .build();
          user2 = RegisterUserDto.builder()
                .name("user2")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .isAuthorized(false)
                        .build())
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                 .username("user2")
                 .password("password")
                .build();
    }

    @Test
    @DisplayName("로그인한 유저는 커뮤니티 글을 작성할 수 있어야 한다.")
    void writePost() throws Exception, UsernameAlreadyExistException {
        SecurityUserDto saved1 = commonUserService.registerUser(user1);
        SecurityUserDto saved2 = commonUserService.registerUser(user2);

        WritePostDto postDto = WritePostDto.builder()
                .title("title")
                .content("content")
                .createdAt(LocalDateTime.now())
                .build();

        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user1&password=password"))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(contentAsString, LoginResult.class);
        String accessToken = loginResult.getSerializedData().get("access token");
        Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");


        mockMvc.perform(post("/community")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .cookie(refreshToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andDo(print());
    }
    @NoArgsConstructor
    @Data
    private static class LoginResult {
        private String status;
        private String message;
        private Map<String, String> serializedData;
    }


    @Test
    void findPost() {
    }

    @Test
    void updatePost() {
    }

    @Test
    void deletePost() {
    }
}