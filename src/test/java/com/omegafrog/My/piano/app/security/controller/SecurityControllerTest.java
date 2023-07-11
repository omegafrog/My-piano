package com.omegafrog.My.piano.app.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import com.omegafrog.My.piano.app.user.entity.UserRepository;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import jakarta.servlet.http.Cookie;
import lombok.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;


import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
class SecurityControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private SecurityUserRepository securityUserRepository;

    @Autowired
    private CommonUserService commonUserService;

    private RegisterUserDto dto;

    @Autowired
    private UserRepository userRepository;


    @AfterEach
    void deleterepository() {
        securityUserRepository.deleteAll();
    }

    @BeforeEach
    void makeDto(){
        dto = RegisterUserDto.builder()
                .username("username")
                .password("password")
                .name("user1")
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder().
                        phoneNum("010-1111-2222")
                        .isAuthorized(false)
                        .build())
                .build();
    }

    @Test
    @DisplayName("/user/register로 유저 회원가입을 할 수 있어야 한다.")
    void registerUserTest() throws Exception, UsernameAlreadyExistException {
        String s = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(s)
                )
                .andExpect(status().isOk())
                .andDo(print());
    }


    @Test
    @DisplayName("중복된 username으로 회원가입시 회원가입에 실패해야 한다.")
    void usernameExistTest() throws UsernameAlreadyExistException, Exception {

        SecurityUserDto securityUserDto = commonUserService.registerUser(dto);
        String s = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(s)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andDo(print());

    }

    @Test
    @DisplayName("유저는 자신을 인증하고  토큰을 발급받아야 한다.")
    void loginTest() throws Exception, UsernameAlreadyExistException {
        SecurityUserDto securityUserDto = commonUserService.registerUser(dto);
        String s = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=username&password=password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200 OK"))
                .andDo(print());
    }

    @Test
    @DisplayName("유저가 로그인에 실패할 경우 올바른 에러를 리턴해야 한다.")
    void loginFailedTest() throws Exception, UsernameAlreadyExistException {
        SecurityUserDto securityUserDto = commonUserService.registerUser(dto);
        String s = objectMapper.writeValueAsString(dto);
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=username1&password=password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andDo(print());
    }

    // TODO : 로그아웃하려면 블랙리스트를 만들어야 하는데 이거는 추가로 repository를 넣어야 해서 힘들듯?
    @Test
    @DisplayName("유저는 로그아웃 할 수 있어야 한다.")
    void logoutTest() throws Exception, UsernameAlreadyExistException {
        SecurityUserDto securityUserDto = commonUserService.registerUser(dto);
        String s = objectMapper.writeValueAsString(dto);
        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=username&password=password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200 OK"))
                .andReturn();
        Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");
        String s2 = mvcResult.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(s2, LoginResult.class);
        Map<String, String> serializedData = loginResult.getSerializedData();
        String accessToken = serializedData.get("access token");

        MvcResult logoutResult = mockMvc.perform(get("/user/logout")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andDo(print())
                .andReturn();

        mockMvc.perform(get("/user/someMethod")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andDo(print());
    }
    @Test
    @DisplayName("인증되지 않은 사용자는 entryPoint로 가야 한다.")
    void entryPointTest() throws Exception {
        mockMvc.perform(get("/user/someMethod"))
                .andExpect(status().isOk())
                .andDo(print());

    }

    @Test
    @DisplayName("로그인한 사용자는 회원탈퇴할 수 있어야 한다.")
    void signOutTest() throws Exception, UsernameAlreadyExistException {
        SecurityUserDto securityUserDto = commonUserService.registerUser(dto);
        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=username&password=password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("200 OK"))
                .andReturn();
        Cookie refreshToken = mvcResult.getResponse().getCookie("refreshToken");
        String s2 = mvcResult.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(s2, LoginResult.class);
        Map<String, String> serializedData = loginResult.getSerializedData();
        String accessToken = serializedData.get("access token");

        MvcResult mvcResult2 = mockMvc.perform(get("/user/signOut")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(jsonPath("$.status").value("200 OK"))
                .andDo(print())
                .andReturn();

        // sercurityUser가 삭제되었는지 확인
        Assertions.assertThatThrownBy(() -> commonUserService.loadUserByUsername(securityUserDto.getUsername()))
                .isInstanceOf(UsernameNotFoundException.class);

        // SercurityUser가 가지고 있는 User 객체 또한 전이되어 삭제되었는지 확인
        Assertions.assertThat(userRepository.count()).isEqualTo(0);

        // 회원탈퇴이후 로그인 실패하는지 확인
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=username&password=password"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("400 BAD_REQUEST"))
                .andDo(print());
    }

    @NoArgsConstructor
    @Data
    private static class LoginResult {
        private String status;
        private String message;
        private Map<String, String> serializedData;
    }

}