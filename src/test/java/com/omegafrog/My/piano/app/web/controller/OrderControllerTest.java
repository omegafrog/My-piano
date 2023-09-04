package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Authority;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonInformation;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.VideoInformation;
import com.omegafrog.My.piano.app.web.domain.order.OrderRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.enums.*;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CommonUserService commonUserService;
    @Autowired
    SheetPostRepository sheetPostRepository;
    @Autowired
    LessonRepository lessonRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private SecurityUserRepository securityUserRepository;
    User testUser1Profile;
    User artist;
    Lesson savedLesson;
    SheetPost savedSheetPost;
    String accessToken;
    Cookie refreshToken;

    @Getter
    @Setter
    private static class LoginResult {
        private String status;
        private String message;
        private Map<String, String> serializedData;
    }

    @BeforeAll
    void register() throws Exception, UsernameAlreadyExistException {
        securityUserRepository.deleteAll();
        RegisterUserDto user1 = RegisterUserDto.builder()
                .name("testUser1")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .build())
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                .username("username")
                .password("password")
                .email("test@gmail.com")
                .build();
        SecurityUserDto securityUserDto1 = commonUserService.registerUser(user1);
        testUser1Profile = ((SecurityUser) commonUserService.loadUserByUsername(securityUserDto1.getUsername()))
                .getUser();
        testUser1Profile.addCash(20000);
        userRepository.save(testUser1Profile);
        RegisterUserDto user2 = RegisterUserDto.builder()
                .name("artist1")
                .email("test@gmail.com")
                .username("username1")
                .password("password")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
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
    }

    @BeforeAll
    @Transactional
    void saveEntity() {
        SheetPost sheetPost = SheetPost.builder()
                .sheet(Sheet.builder()
                        .title("title")
                        .filePath("path1")
                        .genre(Genre.BGM)
                        .user(testUser1Profile)
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

        savedSheetPost = sheetPostRepository.save(sheetPost);
        Lesson lesson = Lesson.builder()
                .sheet(savedSheetPost.getSheet())
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

        savedLesson = lessonRepository.save(lesson);
    }

    @AfterEach
    void clearRepository() {
        orderRepository.deleteAll();
    }

    @AfterAll
    void clearAllRepository() {
        lessonRepository.deleteAll();
        sheetPostRepository.deleteAll();
        securityUserRepository.deleteAll();
        System.out.println("lesson : " + lessonRepository.count());
        System.out.println("sheetPost : " + sheetPostRepository.count());
        System.out.println("user : " + userRepository.count());
    }


    @Test
    @Transactional
    void orderSheet() throws Exception {

        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .buyerId(testUser1Profile.getId())
                .build();

        String data = objectMapper.writeValueAsString(orderDto);
        mockMvc.perform(post("/sheet/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200"))
                .andDo(print());

        Optional<User> byId = userRepository.findById(testUser1Profile.getId());
        Assertions.assertThat(byId.get().getPurchasedSheets().size()).isGreaterThan(0);
        Assertions.assertThat(byId.get().getPurchasedSheets().get(0)).isEqualTo(savedSheetPost);
    }


    @Test
    @Transactional
    void orderLesson() throws Exception {
        OrderRegisterDto order2 = OrderRegisterDto.builder()
                .buyerId(testUser1Profile.getId())
                .itemId(savedLesson.getId())
                .build();

        mockMvc.perform(post("/lesson/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order2))
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200"))
                .andDo(print());
        Optional<User> byId = userRepository.findById(testUser1Profile.getId());
        Assertions.assertThat(byId.get().getPurchasedLessons().size()).isGreaterThan(0);
        Assertions.assertThat(byId.get().getPurchasedLessons().get(0)).isEqualTo(savedLesson);
    }

    @Test
    @Transactional
    void cancelOrderTest() throws Exception {
        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .buyerId(testUser1Profile.getId())
                .build();
        String data = objectMapper.writeValueAsString(orderDto);
        MvcResult mvcResult = mockMvc.perform(post("/sheet/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200"))
                .andDo(print())
                .andReturn();
        String text = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("serializedData").asText();
        Long id = objectMapper.readTree(text).get("order").get("id").asLong();


        mockMvc.perform(get("/order/" + id + "/cancel")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200"))
                .andDo(print());

        Assertions.assertThat(orderRepository.findById(1L)).isEmpty();
    }

    @Test
    @Transactional
    void getAllOrdersTest() throws Exception {
        Optional<SheetPost> bySheetId = sheetPostRepository.findById(savedSheetPost.getId());
        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .buyerId(testUser1Profile.getId())
                .build();
        String data = objectMapper.writeValueAsString(orderDto);
        mockMvc.perform(post("/sheet/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200"))
                .andDo(print());
        SecurityContext context = SecurityContextHolder.getContext();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("username", "password",
                Arrays.asList(Authority.builder().authority("USER").build()));
        token.setDetails(testUser1Profile);
        context.setAuthentication(token);


        MvcResult mvcResult = mockMvc.perform(get("/order")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                )
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200"))
                .andDo(print())
                .andReturn();
        String result = mvcResult.getResponse().getContentAsString();
        String text = objectMapper.readTree(result).get("serializedData").asText();
        System.out.println("text = " + text);
    }

    @Test
    @Transactional
    void createOrderValidationFailTest() throws Exception {
        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .build();
        String data = objectMapper.writeValueAsString(orderDto);
        mockMvc.perform(post("/sheet/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andDo(print());
    }
}