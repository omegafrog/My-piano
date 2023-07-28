package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.dto.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import jakarta.servlet.http.Cookie;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SheetPostControllerTest {

    @Autowired
    private SecurityUserRepository securityUserRepository;
    @Autowired
    private CommonUserService commonUserService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SheetPostRepository sheetPostRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;


    private User user;
    private User artist;
    private String accessToken;
    private Cookie refreshToken;

    RegisterSheetPostDto build;

    @NoArgsConstructor
    @Data
    private static class LoginResult {
        private String status;
        private String message;
        private Map<String, String> serializedData;
    }

    @BeforeAll
    void login() throws Exception, UsernameAlreadyExistException {
        securityUserRepository.deleteAll();
        RegisterUserDto user1 = RegisterUserDto.builder()
                .name("testUser1")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .isAuthorized(false)
                        .build())
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                .username("username")
                .password("password")
                .email("test@gmail.com")
                .build();
        SecurityUserDto securityUserDto1 = commonUserService.registerUser(user1);
        user = ((SecurityUser) commonUserService.loadUserByUsername(securityUserDto1.getUsername()))
                .getUser();
        user.addCash(20000);
        userRepository.save(user);
        RegisterUserDto user2 = RegisterUserDto.builder()
                .name("artist1")
                .email("test@gmail.com")
                .username("username1")
                .password("password")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .isAuthorized(true)
                        .build())
                .build();
        SecurityUserDto securityUserDto2 = commonUserService.registerUser(user2);
        artist = ((SecurityUser) commonUserService.loadUserByUsername(securityUserDto2.getUsername()))
                .getUser();
        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=username&password=password"))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(contentAsString, LoginResult.class);
        accessToken = loginResult.getSerializedData().get("access token");
        refreshToken = mvcResult.getResponse().getCookie("refreshToken");
        build = RegisterSheetPostDto.builder()
                .title("title")
                .content("content")
                .price(12000)
                .sheetDto(
                        Sheet.builder()
                                .title("title")
                                .filePath("path1")
                                .genre(Genre.BGM)
                                .user(artist)
                                .difficulty(Difficulty.MEDIUM)
                                .instrument(Instrument.GUITAR_ACOUSTIC)
                                .isSolo(true)
                                .lyrics(false)
                                .pageNum(3)
                                .build().toSheetDto()
                )
                .artistId(artist.getId())
                .discountRate(0d)
                .build();
    }

    //        @AfterEach
//        void deleteSheetPostRepository(){
//            sheetPostRepository.deleteAll();
//        }
    @Test
    void saveTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        String serializedData = objectMapper.readTree(contentAsString).get("serializedData").asText();
        String content = objectMapper.readTree(serializedData).get("sheetPost").get("content").asText();
        Assertions.assertThat(content).isEqualTo("content");
    }

    @Test
    void updateTest() throws Exception {
//            given
        MvcResult mvcResult = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        String serializedData = objectMapper.readTree(contentAsString).get("serializedData").asText();
        long id = objectMapper.readTree(serializedData).get("sheetPost").get("id").asLong();

        //when
        UpdateSheetPostDto updateBuild = UpdateSheetPostDto.builder()
                .title("changed")
                .content("changedContent")
                .sheetDto(UpdateSheetDto.builder()
                        .title("changedSheet")
                        .filePath("path1")
                        .genre(Genre.BGM)
                        .difficulty(Difficulty.MEDIUM)
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .isSolo(true)
                        .lyrics(false)
                        .pageNum(3)
                        .build())
                .price(11000)
                .discountRate(10d)
                .build();
        MvcResult mvcResult1 = mockMvc.perform(post("/sheet/" + id)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBuild)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        String contentAsString1 = mvcResult1.getResponse().getContentAsString();
        String text = objectMapper.readTree(contentAsString1).get("serializedData").asText();
        String updatedContent = objectMapper.readTree(text).get("sheetPost").get("content").asText();
        long updatedId = objectMapper.readTree(text).get("sheetPost").get("id").asLong();
        Assertions.assertThat(updatedId).isEqualTo(id);
        Assertions.assertThat(updatedContent).isEqualTo("changedContent");
    }

    @Test
    void deleteTest() throws Exception {
        //given
        MvcResult mvcResult = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        String serializedData = objectMapper.readTree(contentAsString).get("serializedData").asText();
        long id = objectMapper.readTree(serializedData).get("sheetPost").get("id").asLong();
        //when
        MvcResult mvcResult1 = mockMvc.perform(delete("/sheet/" + id)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        //then
        mockMvc.perform(get("/sheet/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.toString()))
                .andDo(print());
    }

    @Test
    void findTest() throws Exception {
        //given
        MvcResult mvcResult = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        String serializedData = objectMapper.readTree(contentAsString).get("serializedData").asText();
        long id = objectMapper.readTree(serializedData).get("sheetPost").get("id").asLong();

        MvcResult mvcResult1 = mockMvc.perform(get("/sheet/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        String contentAsString1 = mvcResult1.getResponse().getContentAsString();
        String text = objectMapper.readTree(contentAsString1).get("serializedData").asText();
        long id1 = objectMapper.readTree(text).get("sheetPost").get("id").asLong();
        Assertions.assertThat(id).isEqualTo(id1);
    }

    @Test
    void findAllTest() throws Exception {
        //given
        MvcResult mvcResult1 = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        String contentAsString1 = mvcResult1.getResponse().getContentAsString();
        String serializedData1 = objectMapper.readTree(contentAsString1).get("serializedData").asText();
        long id1 = objectMapper.readTree(serializedData1).get("sheetPost").get("id").asLong();
        build.setTitle("title2");
        MvcResult mvcResult2 = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        String contentAsString2 = mvcResult2.getResponse().getContentAsString();
        String serializedData2 = objectMapper.readTree(contentAsString2).get("serializedData").asText();
        long id2 = objectMapper.readTree(serializedData2).get("sheetPost").get("id").asLong();

        //when
        MvcResult result = mockMvc.perform(get("/sheet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
        long id = objectMapper.readTree(text).get("sheetPosts").get(0).get("id").asLong();
        Assertions.assertThat(id1).isEqualTo(id);
    }

    @AfterAll
    void deleteRepository() {
        System.out.println("sheetPostRepository.count() = " + sheetPostRepository.count());
        // transactional 걸으니까 sheet foreign key땜에 삭제못한다는 오류 없어짐. 왜일가
        sheetPostRepository.deleteAll();
        securityUserRepository.deleteAll();
    }

}