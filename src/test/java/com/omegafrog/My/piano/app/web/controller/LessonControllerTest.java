package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.web.dto.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonRegisterDto;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
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
    private UserRepository userRepository;
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
    SecurityUser savedSecurityUser;


    @BeforeAll
    void settings(){
        System.out.println("Lesson : securityUSerRepository.count() = " + securityUserRepository.count());
        List<SecurityUser> all = securityUserRepository.findAll();
        all.forEach(user -> System.out.println("user = " + user));
        securityUserRepository.deleteAll();
        User a = User.builder()
                .name("artist1")
                .cart(new Cart())
                .email("artist@Gmail.com")
                .cash(20000)
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-1111")
                        .isAuthorized(true)
                        .build())
                .profileSrc("src")
                .build();
        SecurityUser securityUser = SecurityUser.builder()
                .username("username")
                .password("password")
                .user(artist)
                .role(Role.USER)
                .build();
        savedSecurityUser = securityUserRepository.save(securityUser);
        artist = savedSecurityUser.getUser();

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

        userRepository.deleteAll();
    }

    @Nested
    class NeedLoginTest{
        RegisterUserDto user1;
        RegisterUserDto user2;
        String accessToken;
        Cookie refreshToken;
        @NoArgsConstructor
        @Data
        private static class LoginResult {
            private String status;
            private String message;
            private Map<String, String> serializedData;
        }
        @BeforeEach
        void login() throws UsernameAlreadyExistException, Exception {
            SecurityContextHolder.clearContext();
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
                    .email("user1@gmail.com")
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
                    .email("user1@gmail.com")
                    .build();
            SecurityUserDto saved1 = commonUserService.registerUser(user1);
            SecurityUserDto saved2 = commonUserService.registerUser(user2);
            MvcResult mvcResult = mockMvc.perform(post("/user/login")
                            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                            .content("username=user1&password=password"))
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            LoginResult loginResult = objectMapper.readValue(contentAsString, LoginResult.class);
            accessToken = loginResult.getSerializedData().get("access token");
            refreshToken = mvcResult.getResponse().getCookie("refreshToken");
        }
        @AfterEach
        void logout(){
            SecurityContextHolder.clearContext();
        }
        @Test
        @Transactional
        void createLessonTest() throws Exception {
            LessonRegisterDto lessonRegisterDto = LessonRegisterDto.builder()
                    .sheetId(lesson.getSheet().getId())
                    .title(lesson.getTitle())
                    .videoInformation(lesson.getVideoInformation())
                    .lessonInformation(lesson.getLessonInformation())
                    .price(lesson.getPrice())
                    .subTitle(lesson.getSubTitle())
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
        void updateLessonTest() throws Exception{

            Lesson savedLesson = lessonRepository.save(lesson);
            UpdateLessonDto updatedLessonDto = UpdateLessonDto.builder()
                    .lessonInformation(lesson.getLessonInformation())
                    .sheet(lesson.getSheet())
                    .videoInformation(lesson.getVideoInformation())
                    .price(30000)
                    .title("changedTitle")
                    .subTitle(lesson.getSubTitle())
                    .build();
            MvcResult mvcResult = mockMvc.perform(post("/lesson/" + savedLesson.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatedLessonDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                    .andDo(print())
                    .andReturn();
            String contentAsString = mvcResult.getResponse().getContentAsString();
            String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
            long id = objectMapper.readTree(text).get("lesson").get("id").asLong();
            Optional<Lesson> byId = lessonRepository.findById(id);
            Assertions.assertThat(byId).isPresent();
            Assertions.assertThat(byId.get()).isEqualTo(savedLesson);
            Assertions.assertThat(byId.get().getTitle()).isEqualTo("changedTitle");
        }

        @Test
        @Transactional
        void deleteLessonTest() throws Exception {
            Lesson savedLesson = lessonRepository.save(lesson);
            mockMvc.perform(delete("/lesson/" + savedLesson.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(HttpStatus.OK.toString()))
                    .andDo(print());
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
        Lesson saved2 = lessonRepository.save(lesson2);
        MvcResult mvcResult = mockMvc.perform(get("/lesson"))
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