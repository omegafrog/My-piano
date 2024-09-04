package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.RegisterLessonDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import com.omegafrog.My.piano.app.web.enums.*;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@AutoConfigureMockMvc
public class TestUtil {
    @Autowired
    private ObjectMapper objectMapper;


    @AllArgsConstructor
    @Getter
    public class TokenResponse {
        String accessToken;
        Cookie refreshToken;
    }

    public static RegisterUserDto user1 = RegisterUserDto.builder()
            .name("user1")
            .phoneNum("010-1111-2222")
            .profileSrc("src")
            .loginMethod(LoginMethod.EMAIL)
            .email("email1@email.com")
            .username("user1")
            .password("password")
            .build();
    public static RegisterUserDto user2 = RegisterUserDto.builder()
            .name("user2")
            .phoneNum("010-1111-2222")
            .profileSrc("src")
            .email("email2@email.com")
            .loginMethod(LoginMethod.EMAIL)
            .username("user2")
            .password("password")
            .build();

    public static RegisterUserDto user3 = RegisterUserDto.builder()
            .name("user3")
            .phoneNum("010-1111-2222")
            .profileSrc("src")
            .email("email3@email.com")
            .loginMethod(LoginMethod.EMAIL)
            .username("user3")
            .password("password")
            .build();

    public static RegisterSheetPostDto registerSheetPostDto(RegisterSheetDto sheetDto) {
        return RegisterSheetPostDto.builder()
                .title("title")
                .content("content")
                .price(12000)
                .sheetDto(sheetDto)
                .discountRate(0d)
                .build();
    }

    public static RegisterLessonDto registerLessonDto(Long sheetId) {
        return RegisterLessonDto.builder()
                .title("title")
                .sheetId(sheetId)
                .price(2000)
                .lessonInformation(LessonInformation.builder()
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .lessonDescription("hoho")
                        .category(Category.ACCOMPANIMENT)
                        .artistDescription("god")
                        .policy(RefundPolicy.REFUND_IN_7DAYS)
                        .build())
                .videoInformation(
                        VideoInformation.builder()
                                .videoUrl("url")
                                .runningTime(LocalTime.of(0, 20)).build())
                .subTitle("subtitle")
                .build();
    }

    public SheetPostDto writeSheetPost(MockMvc mockMvc, TokenResponse tokens, RegisterSheetPostDto dto) throws Exception {
        MockMultipartFile file = new MockMultipartFile("sheetFiles", "img.pdf", "application/pdf",
                new FileInputStream("src/test/sheet.pdf"));
        String contentAsString = mockMvc.perform(multipart("/api/v1/sheet-post")
                        .file(file)
                        .part(new MockPart("sheetInfo", objectMapper.writeValueAsString(dto).getBytes(
                                StandardCharsets.UTF_8
                        )))
                        .header(HttpHeaders.AUTHORIZATION, tokens.getAccessToken())
                        .cookie(tokens.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.convertValue(objectMapper.readTree(contentAsString).get("data"),
                SheetPostDto.class);
    }

    public LessonDto writeLesson(MockMvc mockMvc, TokenResponse tokens, RegisterLessonDto dto) throws Exception {
        String contentAsString = mockMvc.perform(post("/api/v1/lessons")
                        .header(HttpHeaders.AUTHORIZATION, tokens.getAccessToken())
                        .cookie(tokens.getRefreshToken())
                        .content(objectMapper.writeValueAsString(dto))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        return objectMapper.convertValue(objectMapper.readTree(contentAsString).get("data"), LessonDto.class);
    }


    public static final String sheetUrl = "hi";
    public static RegisterSheetDto registerSheetDto1 =
            RegisterSheetDto.builder()
                    .title("title")
                    .instrument(Instrument.GUITAR_ACOUSTIC.ordinal())
                    .lyrics(false)
                    .filePath(sheetUrl)
                    .difficulty(Difficulty.MEDIUM.ordinal())
                    .genres(new Genres(Genre.BGM, Genre.CAROL))
                    .isSolo(true)
                    .build();
    public static RegisterSheetDto registerSheetDto2 =
            RegisterSheetDto.builder()
                    .title("title")
                    .instrument(Instrument.GUITAR_ACOUSTIC.ordinal())
                    .lyrics(false)
                    .filePath(sheetUrl)
                    .difficulty(Difficulty.MEDIUM.ordinal())
                    .genres(new Genres(Genre.BGM, Genre.CAROL))
                    .isSolo(true)
                    .build();

    public MvcResult chargeCash(MockMvc mockMvc, int amount, TokenResponse tokenResponse) throws Exception {
        return mockMvc.perform(get("/api/v1/cash/charge")
                        .param("amount", String.valueOf(amount))
                        .header(HttpHeaders.AUTHORIZATION, tokenResponse.getAccessToken())
                        .cookie(tokenResponse.getRefreshToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
    }

    public List<SheetPostDto> getPurchasedSheetPosts(MockMvc mockMvc, TokenResponse tokens) throws Exception {
        String contentAsString = mockMvc.perform(get("/api/v1/user/purchasedSheets")
                        .header(HttpHeaders.AUTHORIZATION, tokens.getAccessToken())
                        .cookie(tokens.getRefreshToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        List<SheetPostDto> sheetPostDtos = new ArrayList<>();
        objectMapper.readTree(contentAsString).get("data").forEach(item -> sheetPostDtos.add(
                objectMapper.convertValue(item, SheetPostDto.class)
        ));
        return sheetPostDtos;
    }

    public List<LessonDto> getPurchasedLessons(MockMvc mockMvc, TokenResponse user1Tokens) throws Exception {
        String contentAsString = mockMvc.perform(get("/api/v1/user/purchasedLessons")
                        .header(HttpHeaders.AUTHORIZATION, user1Tokens.getAccessToken())
                        .cookie(user1Tokens.getRefreshToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        List<LessonDto> lessonDtos = new ArrayList<>();
        objectMapper.readTree(contentAsString).get("data").forEach(item ->
                lessonDtos.add(objectMapper.convertValue(item, LessonDto.class)));
        return lessonDtos;
    }

    public static Lesson lesson(SheetPost sheet, User artist) {
        return Lesson.builder()
                .sheetPost(sheet)
                .title("lesson1")
                .price(2000)
                .lessonInformation(LessonInformation.builder()
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .lessonDescription("hoho")
                        .category(Category.ACCOMPANIMENT)
                        .artistDescription("god")
                        .policy(RefundPolicy.REFUND_IN_7DAYS)
                        .build())
                .lessonProvider(artist)
                .subTitle("this is subtitle")
                .videoInformation(
                        VideoInformation.builder()
                                .videoUrl("url")
                                .runningTime(LocalTime.of(0, 20))
                                .build())
                .build();
    }

    public static Sheet sheet(User artist) {
        return Sheet.builder()
                .title("title")
                .sheetUrl("path1")
                .genres(Genres.builder().genre1(Genre.BGM).build())
                .user(artist)
                .difficulty(Difficulty.MEDIUM)
                .instrument(Instrument.GUITAR_ACOUSTIC)
                .isSolo(true)
                .lyrics(false)
                .pageNum(3)
                .build();
    }

    public TokenResponse login(MockMvc mockMvc, String username, String password) throws Exception {
        String content = "username=" + username + "&password=" + password;
        MvcResult result = mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn();
        String responseBody = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseBody)
                .get("data").get("access token").asText();
        Cookie refreshToken = result.getResponse().getCookie("refreshToken");
        return new TokenResponse(accessToken, refreshToken);
    }

    public void register(MockMvc mockMvc, RegisterUserDto user1) throws Exception {
        String s = objectMapper.writeValueAsString(user1);
        MockMultipartFile registerInfo = new MockMultipartFile("registerInfo", "", "application/json",
                s.getBytes());
        mockMvc.perform(multipart("/api/v1/user/register")
                        .file(registerInfo)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andDo(print());
    }

    public UserInfo getUserInfo(MockMvc mockMvc, TokenResponse tokenResponse) throws Exception {
        String contentAsString = mockMvc.perform(get("/api/v1/user")
                        .header(HttpHeaders.AUTHORIZATION, tokenResponse.getAccessToken())
                        .cookie(tokenResponse.getRefreshToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        return objectMapper.convertValue(objectMapper.readTree(contentAsString).get("data"), UserInfo.class);
    }
}
