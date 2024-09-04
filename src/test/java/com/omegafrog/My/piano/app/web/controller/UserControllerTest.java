package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.Cleanup;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    TestUtil testUtil;

    @Autowired
    private Cleanup cleanup;
    String accessToken;
    Cookie refreshToken;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void getLoginToken() throws Exception {
        RegisterUserDto dto = RegisterUserDto.builder()
                .username("username")
                .password("password")
                .name("user")
                .email("email@email.com")
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum("010-1111-2222")
                .build();
        testUtil.register(mockMvc, dto);
        TestUtil.TokenResponse login = testUtil.login(mockMvc, "username", "password");
        accessToken = login.getAccessToken();
        refreshToken = login.getRefreshToken();
    }

    @AfterAll
    void cleanUp() {
        cleanup.cleanUp();
    }


    @Test
    void getCommunityPostTest() throws Exception {
        //given
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/api/v1/community/posts")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long postId = objectMapper.readTree(string).get("data").get("id").asLong();

        // when
        MvcResult mvcResult = mockMvc.perform(get("/api/v1/community/posts")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        //then
        String result = mvcResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(result).get("data").get("postListDtos").get(0).get("id").asLong();
        Assertions.assertThat(id).isEqualTo(postId);
    }

}