package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.enums.*;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import jakarta.persistence.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
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
    @PersistenceUnit
    EntityManagerFactory emf;
    User testUser1 = User.builder()
            .name("testUser1")
            .cart(new Cart())
            .loginMethod(LoginMethod.EMAIL)
            .cash(20000)
            .phoneNum(PhoneNum.builder()
                    .phoneNum("010-1111-2222")
                    .isAuthorized(true)
                    .build())
            .build();
    User artist = User.builder()
            .name("artist1")
            .cart(new Cart())
            .loginMethod(LoginMethod.EMAIL)
            .phoneNum(PhoneNum.builder()
                    .phoneNum("010-1111-2222")
                    .isAuthorized(true)
                    .build())
            .build();
    @Test
    @Transactional
    @WithMockUser(username = "username", password = "password", roles = {"USER"})
    void orderSheet() throws Exception {
        testUser1 = userRepository.save(testUser1);
        artist = userRepository.save(artist);
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

        SheetPost savedSheetPost = sheetPostRepository.save(sheetPost);
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

        Lesson saved = lessonRepository.save(lesson);
        OrderRegisterDto order2 = OrderRegisterDto.builder()
                .buyerId(testUser1.getId())
                .itemId(saved.getId())
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
        Assertions.assertThat(byId.get().getPurchasedLessons().get(0)).isEqualTo(saved);

    }
}