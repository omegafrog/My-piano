package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.enums.*;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
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

    User artist;
    Lesson lesson;
    SheetPost saved;

    @BeforeAll
    void settings() throws UsernameAlreadyExistException {
        securityUserRepository.deleteAll();
        RegisterUserDto user1 = RegisterUserDto.builder()
                .name("user1")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .build())
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                .username("user1")
                .password("password")
                .email("user1@gmail.com")
                .build();
        RegisterUserDto user2 = RegisterUserDto.builder()
                .name("user2")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .build())
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                .username("user2")
                .password("password")
                .email("user1@gmail.com")
                .build();
        SecurityUserDto securityUserDto = commonUserService.registerUser(user1);
        commonUserService.registerUser(user2);
        artist = ((SecurityUser) commonUserService.loadUserByUsername(securityUserDto.getUsername())).getUser();

        SheetPost sheetPost = SheetPost.builder()
                .sheet(Sheet.builder()
                        .title("title")
                        .filePath("path1")
                        .genre(Genre.BGM)
                        .user(artist)
                        .difficulty(Difficulty.MEDIUM)
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .isSolo(true)
                        .lyrics(false)
                        .pageNum(3)
                        .build())
                .title("SheetPostTItle1")
                .price(12000)
                .artist(artist)
                .content("hihi this is content")
                .build();
        saved = sheetPostRepository.save(sheetPost);
    }
    @BeforeEach
    void setLesson() {
        lesson = Lesson.builder()
                .sheet(saved.getSheet())
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
    @AfterEach
    void clearRepository(){
        lessonRepository.deleteAll();
    }

    @AfterAll
    void clearAllRepository(){
        lessonRepository.deleteAll();
        sheetPostRepository.deleteAll();
        securityUserRepository.deleteAll();
    }

    @Nested
    class NeedLoginTest{
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
        @BeforeEach
        void login() throws Exception {
            MvcResult mvcResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .content("username=user1&password=password"))
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
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

        @Test
        @Transactional
        @DisplayName("로그인하고 lesson을 생성할 수 있어야 한다.")
        void createLessonTest() throws Exception {
            LessonRegisterDto lessonRegisterDto = LessonRegisterDto.builder()
                    .sheetId(lesson.getSheet().getId())
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
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                    .andDo(print());
        }
        @Test
        @Transactional
        @DisplayName("로그인하지 않으면 lesson을 등록할 수 없다.")
        void createLessonAuthorizationTest() throws Exception {
            LessonRegisterDto lessonRegisterDto = LessonRegisterDto.builder()
                    .sheetId(lesson.getSheet().getId())
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
                    .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.toString()))
                    .andDo(print());
        }

        @Test
        @Transactional
        @DisplayName("로그인하고 lesson을 수정할 수 있어야 한다.")
        void updateLessonTest() throws Exception{
            Lesson savedLesson = lessonRepository.save(lesson);
            UpdateLessonDto updatedLessonDto = UpdateLessonDto.builder()
                    .lessonInformation(lesson.getLessonInformation())
                    .sheetId(lesson.getSheet().getId())
                    .videoInformation(lesson.getVideoInformation())
                    .price(30000)
                    .title("changedTitle")
                    .subTitle(lesson.getContent())
                    .build();
            MvcResult mvcResult = mockMvc.perform(post("/lesson/" + savedLesson.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedLessonDto))
                            .header(HttpHeaders.AUTHORIZATION, accessToken)
                            .cookie(refreshToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                    .andDo(print())
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
            long id = objectMapper.readTree(text).get("lesson").get("id").asLong();
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
                    .sheetId(lesson.getSheet().getId())
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
                    .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.toString()));
        }

        @Test
        @DisplayName("작성자가 아닌 유저는 레슨을 삭제할 수 없다")
        void deleteAuthorizationTest() throws Exception {
            Lesson savedLesson = lessonRepository.save(lesson);
            mockMvc.perform(delete("/lesson/" + savedLesson.getId())
                            .header(HttpHeaders.AUTHORIZATION, wrongAccessToken)
                            .cookie(wrongRefreshToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.toString()))
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
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                    .andDo(print());
        }

        @Test
        @Transactional
        @DisplayName("로그인하고 댓글을 작성할 수 있다.")
        void addCommentTest() throws Exception {
            Lesson savedLesson = lessonRepository.save(lesson);
            RegisterCommentDto comment = RegisterCommentDto.builder()
                    .content("comment")
                    .build();
            MvcResult mvcResult = mockMvc.perform(post("/lesson/" + savedLesson.getId() + "/comment")
                            .header(HttpHeaders.AUTHORIZATION, accessToken)
                            .cookie(refreshToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(comment)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                    .andDo(print())
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
            String content = objectMapper.readTree(text).get("comments").get(0).get("content").asText();
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
                    .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.toString()));
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

            MvcResult mvcResult = mockMvc.perform(post("/lesson/" + savedLesson.getId() + "/comment")
                            .header(HttpHeaders.AUTHORIZATION, accessToken)
                            .cookie(refreshToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(comment)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                    .andDo(print())
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
            Long commentId = objectMapper.readTree(text).get("comments").get(0).get("id").asLong();

            MvcResult deleteCommentResult = mockMvc.perform(delete("/lesson/" + savedLesson.getId() + "/comment/" + commentId)
                            .header(HttpHeaders.AUTHORIZATION, accessToken)
                            .cookie(refreshToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                    .andReturn();

            String responseBody = deleteCommentResult.getResponse().getContentAsString();
            String serializedData = objectMapper.readTree(responseBody).get("serializedData").asText();
            JsonNode jsonNode = objectMapper.readTree(serializedData).get("comments");
            Assertions.assertThat(jsonNode).isEmpty();
        }
        @Test
        @DisplayName("남의 댓글은 삭제할 수 없다.")
        void deleteCommentAuthorizationTest() throws Exception {
            Lesson savedLesson = lessonRepository.save(lesson);

            RegisterCommentDto comment = RegisterCommentDto.builder()
                    .content("comment")
                    .build();

            MvcResult mvcResult = mockMvc.perform(post("/lesson/" + savedLesson.getId() + "/comment")
                            .header(HttpHeaders.AUTHORIZATION, accessToken)
                            .cookie(refreshToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(comment)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                    .andDo(print())
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
            Long commentId = objectMapper.readTree(text).get("comments").get(0).get("id").asLong();

            mockMvc.perform(delete("/lesson/" + savedLesson.getId() + "/comment/" + commentId)
                            .header(HttpHeaders.AUTHORIZATION, wrongAccessToken)
                            .cookie(wrongRefreshToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.toString()));
        }
    }



    @Test
    @Transactional
    void findLessonTest() throws Exception {
        Lesson saved1 = lessonRepository.save(lesson);
        MvcResult mvcResult = mockMvc.perform(get("/lesson/" + saved1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        LessonDto lessonDto = objectMapper.convertValue(
                objectMapper.readTree(
                        objectMapper.readTree(contentAsString)
                                .get("serializedData").asText())
                        .get("lesson"),
                LessonDto.class);

        Assertions.assertThat(lessonDto.getId()).isEqualTo(saved1.getId());
        Assertions.assertThat(lessonDto.getTitle()).isEqualTo(saved1.getTitle());
    }
    @Test
    @Transactional
    @DisplayName("존재하지 않는 Lesson을 조회할 수 없다.")
    void findLessonFailedTest() throws Exception {
        mockMvc.perform(get("/lesson/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.toString()))
                .andDo(print());
    }

    @Test
    @Transactional
    void findAllLessonTest() throws Exception {
        Lesson saved1 = lessonRepository.save(lesson);
        Lesson lesson2 = Lesson.builder()
                .sheet(saved.getSheet())
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
                        .param("page","0")
                        .param("size","10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
        List<LessonDto> list = new ArrayList<>();
        Iterator<JsonNode> elements = objectMapper.readTree(text).get("lessons").elements();
        while(elements.hasNext()){
            list.add(objectMapper.convertValue(elements.next(), LessonDto.class));
        }
        Assertions.assertThat(list).size().isGreaterThan(1);
        Assertions.assertThat(list.get(0).getId()).isEqualTo(saved1.getId());
    }


}