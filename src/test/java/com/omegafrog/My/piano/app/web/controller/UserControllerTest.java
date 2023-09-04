package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.service.PostApplicationService;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import jakarta.servlet.http.Cookie;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
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
import org.springframework.transaction.annotation.Transactional;


import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private PostApplicationService postApplicationService;

    @Autowired
    private SecurityUserRepository securityUserRepository;
    @Autowired
    private PostRepository postRepository;

    SecurityUser securityUser;
    User user;

    String accessToken;
    Cookie refreshToken;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    void getLoginToken() throws Exception {
        RegisterUserDto dto = RegisterUserDto.builder()
                .username("username")
                .password("password")
                .name("user")
                .email("email@email.com")
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder().
                        phoneNum("010-1111-2222")
                        .build())
                .build();

        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();


        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=username&password=password"))
                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value(HttpStatus.OK))
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(content).get("serializedData").get("access token").asText();
        refreshToken = mvcResult.getResponse().getCookie("refreshToken");
    }



    @AfterEach
    void clearRepository(){
        securityUserRepository.deleteAll();
        System.out.println("securityUserRepository.count() = " + securityUserRepository.count());
    }

    @Test
    void getCommunityPostTest() throws Exception {
        //given

        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String data = objectMapper.readTree(string).get("serializedData").asText();
        Long postId = objectMapper.readTree(data).get("post").get("id").asLong();

        // when
        MvcResult mvcResult = mockMvc.perform(get("/user/community/posts")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        //then
        String result = mvcResult.getResponse().getContentAsString();
        String text = objectMapper.readTree(result).get("serializedData").asText();
        Long id = objectMapper.readTree(text).get("posts").get(0).get("id").asLong();
        Assertions.assertThat(id).isEqualTo(postId);
    }

}