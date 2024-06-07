package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Authority;
import com.omegafrog.My.piano.app.security.exception.DuplicatePropertyException;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.order.OrderRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.user.SecurityUserDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

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
    void register() throws Exception, DuplicatePropertyException {
        securityUserRepository.deleteAll();
        RegisterUserDto user1 = TestLoginUtil.user1;
        SecurityUserDto securityUserDto1 = commonUserService.registerUserWhitoutProfile(user1);
        testUser1Profile = ((SecurityUser) commonUserService.loadUserByUsername(securityUserDto1.getUsername()))
                .getUser();
        testUser1Profile.chargeCash(20000);
        userRepository.save(testUser1Profile);
        RegisterUserDto user2 = TestLoginUtil.user2;
        SecurityUserDto securityUserDto2 = commonUserService.registerUserWhitoutProfile(user2);
        artist = ((SecurityUser) commonUserService.loadUserByUsername(securityUserDto2.getUsername()))
                .getUser();
        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user1&password=password"))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        System.out.println("contentAsString = " + contentAsString);
        accessToken = objectMapper.readTree(contentAsString).get("serializedData").get("access token").asText();
        refreshToken = mvcResult.getResponse().getCookie("refreshToken");
    }

    @BeforeAll
    @Transactional
    void saveEntity() {
        savedSheetPost = sheetPostRepository.save(DummyData.sheetPost(testUser1Profile));
        Lesson lesson = DummyData.lesson(savedSheetPost.getSheet(), artist);
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
        String response = mockMvc.perform(post("/sheet/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200"))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(response).get("serializedData").get("order").get("id").asLong();

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
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                .andDo(print());
    }
}