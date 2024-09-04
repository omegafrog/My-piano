package com.omegafrog.My.piano.app.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.Cleanup;
import com.omegafrog.My.piano.app.security.exception.DuplicatePropertyException;
import com.omegafrog.My.piano.app.web.controller.TestUtil;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import jakarta.servlet.http.Cookie;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Slf4j
class SecurityControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommonUserService commonUserService;
    @Autowired
    private Cleanup cleanup;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestUtil testUtil;


    @AfterEach
    void cleanUp() {
        cleanup.cleanUp();
    }


    @Test
    @DisplayName("/user/register로 유저 회원가입을 할 수 있어야 한다.")
    void registerUserTest() throws Exception {
        String s = objectMapper.writeValueAsString(TestUtil.user1);
        MockMultipartFile registerInfo = new MockMultipartFile("registerInfo", "", "application/json",
                s.getBytes());
        mockMvc.perform(multipart("/api/v1/user/register")
                        .file(registerInfo)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @DisplayName("중복된 username으로 회원가입시 회원가입에 실패해야 한다.")
    void usernameExistTest() throws Exception {
        String s = objectMapper.writeValueAsString(TestUtil.user1);
        MockMultipartFile registerInfo = new MockMultipartFile("registerInfo", "", "application/json",
                s.getBytes());
        mockMvc.perform(multipart("/api/v1/user/register")
                        .file(registerInfo)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andDo(print());

        mockMvc.perform(multipart("/api/v1/user/register")
                        .file(registerInfo)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().is4xxClientError())
                .andDo(print());
    }

    @Test
    @DisplayName("유저는 자신을 인증하고  토큰을 발급받아야 한다.")
    void loginTest() throws Exception {
        String s = objectMapper.writeValueAsString(TestUtil.user1);
        MockMultipartFile registerInfo = new MockMultipartFile("registerInfo", "", "application/json",
                s.getBytes());
        mockMvc.perform(multipart("/api/v1/user/register")
                        .file(registerInfo)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andDo(print());
        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=" + TestUtil.user1.getUsername() + "&password=" + TestUtil.user1.getPassword()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print());
    }

    @Test
    @DisplayName("유저가 로그인에 실패할 경우 올바른 에러를 리턴해야 한다.")
    void loginFailedTest() throws Exception {
        String s = objectMapper.writeValueAsString(TestUtil.user1);
        MockMultipartFile registerInfo = new MockMultipartFile("registerInfo", "", "application/json",
                s.getBytes());
        mockMvc.perform(multipart("/api/v1/user/register")
                        .file(registerInfo)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andDo(print());
        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=username1&password=password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("400"))
                .andDo(print());
    }

    // TODO : 로그아웃하려면 블랙리스트를 만들어야 하는데 이거는 추가로 repository를 넣어야 해서 힘들듯?
    @Test
    @DisplayName("유저는 로그아웃 할 수 있어야 한다.")
    void logoutTest() throws Exception {
        String s = objectMapper.writeValueAsString(TestUtil.user1);
        MockMultipartFile registerInfo = new MockMultipartFile("registerInfo", "", "application/json",
                s.getBytes());
        mockMvc.perform(multipart("/api/v1/user/register")
                        .file(registerInfo)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andDo(print());
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=" + TestUtil.user1.getUsername() + "&password=" + TestUtil.user1.getPassword()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print())
                .andReturn();
        Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");
        String s2 = mvcResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(s2).get("data").get("access token").asText();

        MvcResult logoutResult = mockMvc.perform(get("/api/v1/user/logout")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print())
                .andReturn();

        mockMvc.perform(get("/api/v1/user/purchasedSheets")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andDo(print());
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 entryPoint로 가야 한다.")
    void entryPointTest() throws Exception {
        mockMvc.perform(get("/api/v1/user/someMethod"))
                .andExpect(status().isUnauthorized())
                .andDo(print());

    }

    @Test
    @DisplayName("로그인한 사용자는 회원탈퇴할 수 있어야 한다.")
    void signOutTest() throws Exception, DuplicatePropertyException {
        String s = objectMapper.writeValueAsString(TestUtil.user1);
        MockMultipartFile registerInfo = new MockMultipartFile("registerInfo", "", "application/json",
                s.getBytes());
        mockMvc.perform(multipart("/api/v1/user/register")
                        .file(registerInfo)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andDo(print());
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=" + TestUtil.user1.getUsername() + "&password=" + TestUtil.user1.getPassword()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print())
                .andReturn();
        Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");
        String s2 = mvcResult.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(s2).get("data").get("access token").asText();

        MvcResult mvcResult2 = mockMvc.perform(get("/api/v1/user/signOut")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(jsonPath("$.status").value("200"))
                .andDo(print())
                .andReturn();

        // sercurityUser가 삭제되었는지 확인
        Assertions.assertThatThrownBy(() -> commonUserService.loadUserByUsername(TestUtil.user1.getUsername()))
                .isInstanceOf(UsernameNotFoundException.class);

        // SercurityUser가 가지고 있는 User 객체 또한 전이되어 삭제되었는지 확인
        Assertions.assertThat(userRepository.count()).isEqualTo(0);

        // 회원탈퇴이후 로그인 실패하는지 확인
        mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=username&password=password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("400"))
                .andDo(print());
    }
}