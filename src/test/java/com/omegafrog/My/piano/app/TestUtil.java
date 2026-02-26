package com.omegafrog.My.piano.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.notNullValue;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

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
import com.omegafrog.My.piano.app.web.enums.Category;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.enums.RefundPolicy;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Getter;

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

  public SheetPostDto writeSheetPost(MockMvc mockMvc, MockHttpSession session, RegisterSheetPostDto dto)
      throws Exception {

    MockMultipartFile file = new MockMultipartFile("file", "img.pdf", "application/pdf",
        new FileInputStream("src/test/sheet.pdf"));
    String uploadIdString = mockMvc.perform(multipart("/api/v1/files/upload")
        .file(file)
        .contentType(MediaType.MULTIPART_FORM_DATA)
        .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        .andReturn().getResponse().getContentAsString();
    String uploadId = objectMapper.readTree(uploadIdString).get("data").get("uploadId").asText();
    dto.setUploadId(uploadId);
    Thread.sleep(5000);

    String contentAsString = mockMvc.perform(post("/api/v1/sheet-post").content(
        objectMapper.writeValueAsString(dto))
        .contentType("application/json")
        .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        .andDo(print())
        .andReturn().getResponse().getContentAsString();

    return objectMapper.convertValue(objectMapper.readTree(contentAsString).get("data"),
        SheetPostDto.class);
  }

  public LessonDto writeLesson(MockMvc mockMvc, MockHttpSession session, RegisterLessonDto dto) throws Exception {
    String contentAsString = mockMvc.perform(post("/api/v1/lessons")
        .session(session)
        .content(objectMapper.writeValueAsString(dto))
        .contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        .andReturn().getResponse().getContentAsString();

    return objectMapper.convertValue(objectMapper.readTree(contentAsString).get("data"), LessonDto.class);
  }

  public static final String sheetUrl = "hi";
  public static RegisterSheetDto registerSheetDto1 = RegisterSheetDto.builder()
      .title("title")
      .instrument(Instrument.GUITAR_ACOUSTIC.ordinal())
      .lyrics(false)
      .difficulty(Difficulty.MEDIUM.ordinal())
      .genres(new Genres(Genre.BGM, Genre.CAROL))
      .isSolo(true)
      .build();
  public static RegisterSheetDto registerSheetDto2 = RegisterSheetDto.builder()
      .title("title")
      .instrument(Instrument.GUITAR_ACOUSTIC.ordinal())
      .lyrics(false)
      .difficulty(Difficulty.MEDIUM.ordinal())
      .genres(new Genres(Genre.BGM, Genre.CAROL))
      .isSolo(true)
      .build();

  public MvcResult chargeCash(MockMvc mockMvc, int amount, MockHttpSession session) throws Exception {
    return mockMvc.perform(get("/api/v1/cash/charge")
        .param("amount", String.valueOf(amount))
        .session(session))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        .andReturn();
  }

  public List<SheetPostDto> getPurchasedSheetPosts(MockMvc mockMvc, MockHttpSession session) throws Exception {
    String contentAsString = mockMvc.perform(get("/api/v1/user/purchasedSheets")
        .session(session))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        .andReturn().getResponse().getContentAsString();
    List<SheetPostDto> sheetPostDtos = new ArrayList<>();
    objectMapper.readTree(contentAsString).get("data").forEach(item -> sheetPostDtos.add(
        objectMapper.convertValue(item, SheetPostDto.class)));
    return sheetPostDtos;
  }

  public List<LessonDto> getPurchasedLessons(MockMvc mockMvc, MockHttpSession session) throws Exception {
    String contentAsString = mockMvc.perform(get("/api/v1/user/purchasedLessons")
        .session(session))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        .andReturn().getResponse().getContentAsString();

    List<LessonDto> lessonDtos = new ArrayList<>();
    objectMapper.readTree(contentAsString).get("data")
        .forEach(item -> lessonDtos.add(objectMapper.convertValue(item, LessonDto.class)));
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

  public MockHttpSession login(MockMvc mockMvc, String username, String password) throws Exception {
    String content = "username=" + username + "&password=" + password;
    MvcResult result = mockMvc.perform(post("/api/v1/user/login")
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .content(content))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        .andExpect(request().sessionAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, notNullValue()))
        .andDo(print())
        .andReturn();

    return (MockHttpSession) result.getRequest().getSession(false);
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

  public UserInfo getUserInfo(MockMvc mockMvc, MockHttpSession session) throws Exception {
    String contentAsString = mockMvc.perform(get("/api/v1/user")
        .session(session))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        .andReturn().getResponse().getContentAsString();
    return objectMapper.convertValue(objectMapper.readTree(contentAsString).get("data"), UserInfo.class);
  }
}
