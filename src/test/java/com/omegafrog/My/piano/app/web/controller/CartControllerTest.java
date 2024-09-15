package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.Cleanup;
import com.omegafrog.My.piano.app.TestUtil;
import com.omegafrog.My.piano.app.TestUtilConfig;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderRegisterDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import jakarta.persistence.EntityNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestUtilConfig.class)
class CartControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Cleanup cleanup;

    private TestUtil.TokenResponse sellerToken;
    private TestUtil.TokenResponse buyerToken;

    @Autowired
    private SecurityUserRepository securityUserRepository;
    @Autowired
    private TestUtil testUtil;
    @Autowired
    private MockMvc mockMvc;


    @BeforeEach
    public void login() throws Exception {
        // register seller
        testUtil.register(mockMvc, TestUtil.user1);
        SecurityUser securityUser = securityUserRepository.findByUsername(TestUtil.user1.getUsername())
                .orElseThrow(() -> new EntityNotFoundException());
        securityUser.changeRole(Role.CREATOR);
        securityUserRepository.save(securityUser);
        sellerToken =
                testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());

        // register buyer
        testUtil.register(mockMvc, TestUtil.user2);
        buyerToken =
                testUtil.login(mockMvc, TestUtil.user2.getUsername(), TestUtil.user2.getPassword());
    }

    @AfterEach
    void cleanUp() {
        cleanup.cleanUp();
    }

    @Test
    void saveToCartTest() throws Exception {
        // given
        SheetPostDto savedSheetPostDto
                = testUtil.writeSheetPost(mockMvc, sellerToken, TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1));
        LessonDto savedLessonDto
                = testUtil.writeLesson(mockMvc, sellerToken, TestUtil.registerLessonDto(savedSheetPostDto.getSheet().getId()));

        OrderRegisterDto build1 = OrderRegisterDto.builder()
                .itemId(savedSheetPostDto.getId())
                .build();
        OrderRegisterDto build2 = OrderRegisterDto.builder()
                .itemId(savedLessonDto.getId())
                .build();
        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/cart/sheet")
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        //then
        String content = mvcResult.getResponse().getContentAsString();
        String title = objectMapper.readTree(content).get("data").get(0).get("item").get("title").asText();
        Assertions.assertThat(title).isEqualTo(savedSheetPostDto.getTitle());

        MvcResult mvcResult2 = mockMvc.perform(post("/api/v1/cart/lesson")
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String contentAsString = mvcResult2.getResponse().getContentAsString();
        String title2 = objectMapper.readTree(contentAsString).get("data").get(0).get("item").get("title").asText();
        Assertions.assertThat(title2).isEqualTo(savedLessonDto.getTitle());
    }

    @Test
    void deleteFromCartTest() throws Exception {
        //given
        SheetPostDto savedSheetPostDto
                = testUtil.writeSheetPost(mockMvc, sellerToken, TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1));
        OrderRegisterDto build1 = OrderRegisterDto.builder()
                .itemId(savedSheetPostDto.getId())
                .build();
        //when
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/cart/sheet")
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String content = mvcResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(content).get("data").get(0).get("item").get("id").asLong();

        mockMvc.perform(delete("/api/v1/cart/" + id)
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        //then
        mockMvc.perform(get("/api/v1/cart")
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void getAllContentFromCartTest() throws Exception {
        //given
        SheetPostDto savedSheetPostDto
                = testUtil.writeSheetPost(mockMvc, sellerToken, TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1));
        OrderRegisterDto build1 = OrderRegisterDto.builder()
                .itemId(savedSheetPostDto.getId())
                .build();

        //when
        mockMvc.perform(post("/api/v1/cart/sheet")
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        MvcResult mvcResult = mockMvc.perform(get("/api/v1/cart")
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn();

        String content = mvcResult.getResponse().getContentAsString();
        Assertions.assertThat(objectMapper.readTree(content).get("data").get(0).get("item").get("id").asLong())
                .isEqualTo(savedSheetPostDto.getId());
    }

    @Test
    void payAllInCartTest() throws Exception {
        //given
        testUtil.chargeCash(mockMvc, 20000, buyerToken);
        SheetPostDto savedSheetPostDto
                = testUtil.writeSheetPost(mockMvc, sellerToken, TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1));
        LessonDto savedLessonDto
                = testUtil.writeLesson(mockMvc, sellerToken, TestUtil.registerLessonDto(savedSheetPostDto.getSheet().getId()));
        OrderRegisterDto build1 = OrderRegisterDto.builder()
                .itemId(savedSheetPostDto.getId())
                .build();
        OrderRegisterDto build2 = OrderRegisterDto.builder()
                .itemId(savedLessonDto.getId())
                .build();
        mockMvc.perform(post("/api/v1/cart/sheet")
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        mockMvc.perform(post("/api/v1/cart/lesson")
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        mockMvc.perform(patch("/api/v1/cart?orderId=" + build1.getItemId() + "," + build2.getItemId())
                        .header(HttpHeaders.AUTHORIZATION, buyerToken.getAccessToken())
                        .cookie(buyerToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));

        UserInfo userInfo = testUtil.getUserInfo(mockMvc, buyerToken);

        Assertions.assertThat(userInfo.getCash())
                .isLessThan(20000);
    }
}