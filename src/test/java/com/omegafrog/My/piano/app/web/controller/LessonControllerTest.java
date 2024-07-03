package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.user.SecurityUserDto;
import com.omegafrog.My.piano.app.web.dto.lesson.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.enums.*;
import jakarta.servlet.http.Cookie;
import lombok.Data;
import lombok.NoArgsConstructor;
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

import java.time.LocalTime;
import java.util.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class LessonControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SheetPostRepository sheetPostRepository;
    @Autowired
    private SecurityUserRepository securityUserRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private CommonUserService commonUserService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    User artist;
    Lesson lesson;
    SheetPost savedSheetPost;

    @BeforeAll
    void settings() throws Exception {

        SecurityUserDto securityUserDto = commonUserService.registerUserWithoutProfile(TestLoginUtil.user1);
        commonUserService.registerUserWithoutProfile(TestLoginUtil.user2);
        artist = ((SecurityUser) commonUserService.loadUserByUsername(securityUserDto.getUsername())).getUser();

        savedSheetPost = sheetPostRepository.save(DummyData.sheetPost(artist));

        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user1&password=password"))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        System.out.println("contentAsString = " + contentAsString);
        LoginResult loginResult = objectMapper.readValue(contentAsString, LoginResult.class);
        accessToken = loginResult.getSerializedData().get("access token");
        refreshToken = mvcResult.getResponse().getCookie("refreshToken");

        MvcResult mvcResult2 = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user2&password=password"))
                .andReturn();
        String contentAsString2 = mvcResult2.getResponse().getContentAsString();
        LoginResult loginResult2 = objectMapper.readValue(contentAsString2, LoginResult.class);
        wrongAccessToken = loginResult2.getSerializedData().get("access token");
        wrongRefreshToken = mvcResult2.getResponse().getCookie("refreshToken");
    }

    @BeforeEach
    void setLesson() {
        lesson = DummyData.lesson(savedSheetPost, artist);
    }


    String accessToken;
    Cookie refreshToken;

    String wrongAccessToken;
    Cookie wrongRefreshToken;

    @NoArgsConstructor
    @Data
    private static class LoginResult {
        private String status;
        private String message;
        private Map<String, String> serializedData;
    }

    @Test
    @Transactional
    @DisplayName("로그인하고 lesson을 생성할 수 있어야 한다.")
    void createLessonTest() throws Exception {
        LessonRegisterDto lessonRegisterDto = LessonRegisterDto.builder()
                .sheetId(lesson.getSheetPost().getId())
                .title(lesson.getTitle())
                .videoInformation(lesson.getVideoInformation())
                .lessonInformation(lesson.getLessonInformation())
                .price(lesson.getPrice())
                .subTitle(lesson.getContent())
                .build();
        String body = objectMapper.writeValueAsString(lessonRegisterDto);
        System.out.println("body = " + body);

        mockMvc.perform(post("/lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print());
    }

    @Test
    @Transactional
    @DisplayName("로그인하지 않으면 lesson을 등록할 수 없다.")
    void createLessonAuthorizationTest() throws Exception {
        LessonRegisterDto lessonRegisterDto = LessonRegisterDto.builder()
                .sheetId(lesson.getSheetPost().getId())
                .title(lesson.getTitle())
                .videoInformation(lesson.getVideoInformation())
                .lessonInformation(lesson.getLessonInformation())
                .price(lesson.getPrice())
                .subTitle(lesson.getContent())
                .build();
        String body = objectMapper.writeValueAsString(lessonRegisterDto);
        mockMvc.perform(post("/lesson")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                .andDo(print());
    }

    @Test
    @Transactional
    @DisplayName("로그인하고 lesson을 수정할 수 있어야 한다.")
    void updateLessonTest() throws Exception {
        Lesson savedLesson = lessonRepository.save(lesson);
        UpdateLessonDto updatedLessonDto = UpdateLessonDto.builder()
                .lessonInformation(lesson.getLessonInformation())
                .sheetId(lesson.getSheetPost().getId())
                .videoInformation(lesson.getVideoInformation())
                .price(30000)
                .title("changedTitle")
                .subTitle(lesson.getContent())
                .build();
        String contentAsString = mockMvc.perform(post("/lesson/" + savedLesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLessonDto))
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        ;
        long id = objectMapper.readTree(contentAsString).get("serializedData").get("lesson").get("id").asLong();
        Optional<Lesson> byId = lessonRepository.findById(id);
        Assertions.assertThat(byId).isPresent();
        Assertions.assertThat(byId).contains(savedLesson);
        Assertions.assertThat(byId.get().getTitle()).isEqualTo("changedTitle");


    }

    @Test
    @DisplayName("작성자가 아닌 유저는 수정할 수 없다")
    void updateAuthorizationTest() throws Exception {
        Lesson savedLesson = lessonRepository.save(lesson);
        UpdateLessonDto updatedLessonDto = UpdateLessonDto.builder()
                .lessonInformation(lesson.getLessonInformation())
                .sheetId(lesson.getSheetPost().getId())
                .videoInformation(lesson.getVideoInformation())
                .price(30000)
                .title("changedTitle")
                .subTitle(lesson.getContent())
                .build();

        // 작성자가 아닌 유저는 접근할 수 없다.
        mockMvc.perform(post("/lesson/" + savedLesson.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLessonDto))
                        .header(HttpHeaders.AUTHORIZATION, wrongAccessToken)
                        .cookie(wrongRefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("작성자가 아닌 유저는 레슨을 삭제할 수 없다")
    void deleteAuthorizationTest() throws Exception {
        Lesson savedLesson = lessonRepository.save(lesson);
        mockMvc.perform(delete("/lesson/" + savedLesson.getId())
                        .header(HttpHeaders.AUTHORIZATION, wrongAccessToken)
                        .cookie(wrongRefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                .andDo(print());
    }

    @Test
    @Transactional
    @DisplayName("로그인하고 레슨을 삭제할 수 있다.")
    void deleteLessonTest() throws Exception {
        Lesson savedLesson = lessonRepository.save(lesson);
        mockMvc.perform(delete("/lesson/" + savedLesson.getId())
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print());
    }

    @Test
    @Transactional
    @DisplayName("로그인하고 댓글을 작성할 수 있다.")
    void addCommentTest() throws Exception {
        List<RefreshToken> all = refreshTokenRepository.findAll();
        System.out.println("all = " + all);
        Lesson savedLesson = lessonRepository.save(lesson);
        RegisterCommentDto comment = RegisterCommentDto.builder()
                .content("comment")
                .build();
        String contentAsString = mockMvc.perform(post("/lesson/" + savedLesson.getId() + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        ;
        String content = objectMapper.readTree(contentAsString).get("serializedData").get("comments").get(0).get("content").asText();
        Assertions.assertThat(content).isEqualTo("comment");
    }

    @Test
    @DisplayName("로그인하지 않으면 댓글을 작성할 수 없다.")
    void addCommentAuthenticationTest() throws Exception {
        Lesson savedLesson = lessonRepository.save(lesson);
        CommentDto comment = CommentDto.builder()
                .content("comment")
                .build();
        mockMvc.perform(post("/lesson/" + savedLesson.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Transactional
    @DisplayName("자신이 쓴 댓글을 삭제할 수 있다.")
    void deleteCommentTest() throws Exception {
        // given
        Lesson savedLesson = lessonRepository.save(lesson);

        RegisterCommentDto comment = RegisterCommentDto.builder()
                .content("comment")
                .build();

        String contentAsString = mockMvc.perform(post("/lesson/" + savedLesson.getId() + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        ;
        Long commentId = objectMapper.readTree(contentAsString).get("serializedData").get("comments").get(0).get("id").asLong();

        MvcResult deleteCommentResult = mockMvc.perform(delete("/lesson/" + savedLesson.getId() + "/comment/" + commentId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        String responseBody = deleteCommentResult.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(responseBody).get("serializedData").get("comments");
        Assertions.assertThat(jsonNode).isEmpty();
    }

    @Test
    @DisplayName("남의 댓글은 삭제할 수 없다.")
    void deleteCommentAuthorizationTest() throws Exception {
        Lesson savedLesson = lessonRepository.save(lesson);

        RegisterCommentDto comment = RegisterCommentDto.builder()
                .content("comment")
                .build();

        String contentAsString = mockMvc.perform(post("/lesson/" + savedLesson.getId() + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        ;
        Long commentId = objectMapper.readTree(contentAsString).get("serializedData").get("comments").get(0).get("id").asLong();
        mockMvc.perform(delete("/lesson/" + savedLesson.getId() + "/comment/" + commentId)
                        .header(HttpHeaders.AUTHORIZATION, wrongAccessToken)
                        .cookie(wrongRefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Transactional
    void findLessonTest() throws Exception {
        Lesson saved1 = lessonRepository.save(lesson);
        String contentAsString = mockMvc.perform(get("/lesson/" + saved1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();;
        LessonDto lessonDto = objectMapper.convertValue(
                objectMapper.readTree(contentAsString).get("serializedData").get("lesson"), LessonDto.class);

        Assertions.assertThat(lessonDto.getId()).isEqualTo(saved1.getId());
        Assertions.assertThat(lessonDto.getTitle()).isEqualTo(saved1.getTitle());
    }

    @Test
    @Transactional
    @DisplayName("존재하지 않는 Lesson을 조회할 수 없다.")
    void findLessonFailedTest() throws Exception {
        mockMvc.perform(get("/lesson/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    @Transactional
    void findAllLessonTest() throws Exception {
        Lesson saved1 = lessonRepository.save(lesson);
        Lesson lesson2 = Lesson.builder()
                .sheetPost(savedSheetPost)
                .title("lesson2")
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
        lessonRepository.save(lesson2);
        MvcResult mvcResult = mockMvc.perform(get("/lessons")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Iterator<JsonNode> elements = objectMapper.readTree(contentAsString).get("serializedData").get("lessons").elements();
        List<LessonDto> list = new ArrayList<>();
        while (elements.hasNext()) {
            list.add(objectMapper.convertValue(elements.next(), LessonDto.class));
        }
        Assertions.assertThat(list).size().isGreaterThan(1);
        Assertions.assertThat(list.get(0).getId()).isEqualTo(saved1.getId());
    }


}