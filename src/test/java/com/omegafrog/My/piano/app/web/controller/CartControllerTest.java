package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.order.OrderRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.enums.*;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import jakarta.servlet.http.Cookie;
import lombok.Getter;
import lombok.Setter;
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
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CommonUserService userService;
    @Autowired
    private SheetPostRepository sheetPostRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;

    private String accessToken;
    private Cookie refreshToken;
    private SecurityUser user;
    private SheetPost savedSheetPost;
    private Lesson savedLesson;
    @Autowired
    private SecurityUserRepository securityUserRepository;

    @Getter
    @Setter
    private static class LoginResult {
        private String status;
        private String message;
        private Map<String, String> serializedData;
    }

    @BeforeAll
    public void login() throws Exception {
        RegisterUserDto dto = RegisterUserDto.builder()
                .username("username")
                .password("password")
                .name("user")
                .email("email@email.com")
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum("010-1111-2222")
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
                .andDo(print())
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(content, LoginResult.class);
        accessToken = loginResult.getSerializedData().get("access token");
        refreshToken = mvcResult.getResponse().getCookie("refreshToken");
        user = (SecurityUser) userService.loadUserByUsername("username");
    }

    @BeforeAll
    void setItem() {
        SheetPost sheetPost = SheetPost.builder()
                .sheet(Sheet.builder()
                        .title("title")
                        .sheetUrl("path1")
                        .genres(Genres.builder().genre1(Genre.BGM).build())
                        .user(user.getUser())
                        .difficulty(Difficulty.MEDIUM)
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .isSolo(true)
                        .lyrics(false)
                        .pageNum(3)
                        .build())
                .title("SheetPostTitle1")
                .price(12000)
                .artist(user.getUser())
                .content("hihi this is content")
                .build();

        savedSheetPost = sheetPostRepository.save(sheetPost);
        Lesson lesson = Lesson.builder()
                .sheetPost(savedSheetPost)
                .title("lesson1")
                .price(2000)
                .lessonInformation(LessonInformation.builder()
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .lessonDescription("hoho")
                        .category(Category.ACCOMPANIMENT)
                        .artistDescription("god")
                        .policy(RefundPolicy.REFUND_IN_7DAYS)
                        .build())
                .lessonProvider(user.getUser())
                .subTitle("this is subtitle")
                .videoInformation(
                        VideoInformation.builder()
                                .videoUrl("url")
                                .runningTime(LocalTime.of(0, 20))
                                .build())
                .build();
        savedLesson = lessonRepository.save(lesson);
    }

    @Test
    void saveToCartTest() throws Exception {

        // given
        OrderRegisterDto build1 = OrderRegisterDto.builder()
                .buyerId(user.getUser().getId())
                .itemId(savedSheetPost.getId())
                .build();
        OrderRegisterDto build2 = OrderRegisterDto.builder()
                .buyerId(user.getUser().getId())
                .itemId(savedLesson.getId())
                .build();
        //when
        MvcResult mvcResult = mockMvc.perform(post("/cart/sheet")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        //then
        String content = mvcResult.getResponse().getContentAsString();
        String title = objectMapper.readTree(content).get("serializedData").get("contents").get(0).get("item").get("title").asText();
        Assertions.assertThat(title).isEqualTo("SheetPostTitle1");

        MvcResult mvcResult2 = mockMvc.perform(post("/cart/lesson")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String contentAsString = mvcResult2.getResponse().getContentAsString();
        String title2 = objectMapper.readTree(contentAsString).get("serializedData").get("contents").get(1).get("item").get("title").asText();
        Assertions.assertThat(title2).isEqualTo("lesson1");
    }

    @Test
    void deleteFromCartTest() throws Exception {
        OrderRegisterDto build1 = OrderRegisterDto.builder()
                .buyerId(user.getUser().getId())
                .itemId(savedSheetPost.getId())
                .build();
        //when
        MvcResult mvcResult = mockMvc.perform(post("/cart/sheet")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(content).get("serializedData").get("contents").get(0).get("id").asLong();

        mockMvc.perform(delete("/cart/" + id)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
    }

    @Test
    void getAllContentFromCartTest() throws Exception {
        OrderRegisterDto build1 = OrderRegisterDto.builder()
                .buyerId(user.getUser().getId())
                .itemId(savedSheetPost.getId())
                .build();

        //when
        mockMvc.perform(post("/cart/sheet")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/cart")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(content).get("serializedData").get("contents").get(0).get("item").get("id").asLong();
        Assertions.assertThat(id).isEqualTo(savedSheetPost.getId());
    }

    @Test
    void payAllInCartTest() throws Exception {

        mockMvc.perform(post("/user/cash")
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .cookie(refreshToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(20000)));
        OrderRegisterDto build1 = OrderRegisterDto.builder()
                .buyerId(user.getUser().getId())
                .itemId(savedSheetPost.getId())
                .build();
        OrderRegisterDto build2 = OrderRegisterDto.builder()
                .buyerId(user.getUser().getId())
                .itemId(savedLesson.getId())
                .build();
        mockMvc.perform(post("/cart/sheet")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        mockMvc.perform(post("/cart/lesson")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        mockMvc.perform(get("/cart/pay")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));

        Optional<User> byId = userRepository.findById(user.getUser().getId());
        System.out.println("byId.get().getCash() = " + byId.get().getCash());
        Assertions.assertThat(byId.get().getCash()).isLessThan(20000);
    }
}