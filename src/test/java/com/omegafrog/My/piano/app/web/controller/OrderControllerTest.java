package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.authorities.Authority;
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
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.enums.*;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import jakarta.persistence.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
class OrderControllerTest {

    @Autowired
    MockMvc mockMvc;

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


    User testUser1;
    User artist;
    Lesson savedLesson;
    SheetPost savedSheetPost;
    @BeforeEach
    @Transactional
    void saveEntity(){
        User a = User.builder()
                .name("testUser1")
                .email("test@gmail.com")
                .cart(new Cart())
                .loginMethod(LoginMethod.EMAIL)
                .cash(20000)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .isAuthorized(true)
                        .build())
                .build();
        User b = User.builder()
                .name("artist1")
                .email("test@gmail.com")
                .cart(new Cart())
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .isAuthorized(true)
                        .build())
                .build();
        testUser1 = userRepository.save(a);
        artist = userRepository.save(b);
        Lesson lesson = Lesson.builder()
                .sheet(Sheet.builder()
                        .title("title")
                        .filePath("path1")
                        .genre(Genre.BGM)
                        .user(testUser1)
                        .difficulty(Difficulty.MEDIUM)
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .isSolo(true)
                        .lyrics(false)
                        .pageNum(3)
                        .build())
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

        SheetPost sheetPost = SheetPost.builder()
                .sheet(Sheet.builder()
                        .title("title")
                        .filePath("path1")
                        .genre(Genre.BGM)
                        .user(testUser1)
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

        savedLesson= lessonRepository.save(lesson);
        savedSheetPost= sheetPostRepository.save(sheetPost);

    }
    @AfterEach
    void deleteEntity(){
        lessonRepository.deleteAll();
        sheetPostRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @Transactional
    @WithMockUser(username = "username", password = "password", roles = {"USER"})
    void orderSheet() throws Exception {

        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .buyerId(testUser1.getId())
                .build();
        String data = objectMapper.writeValueAsString(orderDto);
        mockMvc.perform(post("/sheet/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200 OK"))
                .andDo(print());

        Optional<User> byId = userRepository.findById(testUser1.getId());
        Assertions.assertThat(byId.get().getPurchasedSheets().size()).isGreaterThan(0);
        Assertions.assertThat(byId.get().getPurchasedSheets().get(0)).isEqualTo(savedSheetPost.getSheet());
    }


    @Test
    @Transactional
    void orderLesson() throws Exception {
        OrderRegisterDto order2 = OrderRegisterDto.builder()
                .buyerId(testUser1.getId())
                .itemId(savedLesson.getId())
                .build();

        mockMvc.perform(post("/lesson/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order2)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200 OK"))
                .andDo(print());
        Optional<User> byId = userRepository.findById(testUser1.getId());
        Assertions.assertThat(byId.get().getCash()).isLessThan(20000);
        Assertions.assertThat(byId.get().getPurchasedLessons().size()).isGreaterThan(0);
        Assertions.assertThat(byId.get().getPurchasedLessons().get(0)).isEqualTo(savedLesson);
    }

    @Test
    @Transactional
    void cancelOrderTest() throws Exception {
        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .buyerId(testUser1.getId())
                .build();
        String data = objectMapper.writeValueAsString(orderDto);
        MvcResult mvcResult = mockMvc.perform(post("/sheet/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200 OK"))
                .andDo(print())
                .andReturn();
        String text = objectMapper.readTree(mvcResult.getResponse().getContentAsString()).get("serializedData").asText();
        Long id = objectMapper.readTree(text).get("order").get("id").asLong();


        mockMvc.perform(get("/order/"+id+"/cancel"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200 OK"))
                .andDo(print());

        Assertions.assertThat(orderRepository.findById(1L)).isEmpty();
    }

    @Test
    @Transactional
    void getAllOrdersTest() throws Exception {
        OrderRegisterDto orderDto = OrderRegisterDto.builder()
                .itemId(savedSheetPost.getId())
                .buyerId(testUser1.getId())
                .build();
        String data = objectMapper.writeValueAsString(orderDto);
        mockMvc.perform(post("/sheet/buy")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(data))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200 OK"))
                .andDo(print());
        SecurityContext context = SecurityContextHolder.getContext();
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("username", "password",
                Arrays.asList(Authority.builder().authority("USER").build()));
        token.setDetails(testUser1);
        context.setAuthentication(token);


        MvcResult mvcResult = mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("200 OK"))
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
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("400 BAD_REQUEST"))
                .andDo(print());
    }


}