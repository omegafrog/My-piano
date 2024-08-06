package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.exception.DuplicatePropertyException;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.user.SecurityUserDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import jakarta.servlet.http.Cookie;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SheetPostControllerTest {

    @Autowired
    private SecurityUserRepository securityUserRepository;
    @Autowired
    private CommonUserService commonUserService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SheetPostRepository sheetPostRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;


    private User artist;
    private String accessToken;
    private Cookie refreshToken;

    RegisterSheetPostDto build;

    @NoArgsConstructor
    @Data
    private static class LoginResult {
        private String status;
        private String message;
        private Map<String, String> serializedData;
    }

    @BeforeEach
    void login() throws Exception{
        SecurityUserDto securityUserDto1 = commonUserService.registerUserWithoutProfile(TestLoginUtil.user1);
        User user = ((SecurityUser) commonUserService.loadUserByUsername(securityUserDto1.getUsername()))
                .getUser();
        user.chargeCash(20000);
        userRepository.save(user);

        SecurityUserDto securityUserDto2 = commonUserService.registerUserWithoutProfile(TestLoginUtil.user2);
        artist = ((SecurityUser) commonUserService.loadUserByUsername(securityUserDto2.getUsername()))
                .getUser();
        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user1&password=password"))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(contentAsString, LoginResult.class);
        accessToken = loginResult.getSerializedData().get("access token");
        refreshToken = mvcResult.getResponse().getCookie("refreshToken");
        build = RegisterSheetPostDto.builder()
                .title("title")
                .content("content")
                .price(12000)
                .sheetDto(DummyData.registerSheetDto(DummyData.sheet(artist)))
                .artistId(artist.getId())
                .discountRate(0d)
                .build();
    }

    @Test
    void saveTest() throws Exception {
        String contentAsString = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();;

        String content = objectMapper.readTree(contentAsString).get("serializedData").get("sheetPost").get("content").asText();
        Assertions.assertThat(content).isEqualTo("content");
    }

    @Test
    void updateTest() throws Exception {
//            given
        String contentAsString = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();;
        Long id = objectMapper.readTree(contentAsString).get("serializedData").get("sheetPost").get("id").asLong();

        //when
        UpdateSheetPostDto updateBuild = UpdateSheetPostDto.builder()
                .title("changed")
                .content("changedContent")
                .sheet(UpdateSheetDto.builder()
                        .title("changedSheet")
                        .sheetUrl("path1")
                        .genres(Genres.builder().genre1(Genre.BGM).build())
                        .difficulty(Difficulty.MEDIUM)
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .isSolo(true)
                        .lyrics(false)
                        .pageNum(3)
                        .build())
                .price(11000)
                .discountRate(10d)
                .build();
        MvcResult mvcResult1 = mockMvc.perform(post("/sheet/" + id)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBuild)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String contentAsString1 = mvcResult1.getResponse().getContentAsString();
        JsonNode sheetPost = objectMapper.readTree(contentAsString1).get("serializedData").get("sheetPost");
        String updatedContent = sheetPost.get("content").asText();
        long updatedId = sheetPost.get("id").asLong();
        Assertions.assertThat(updatedId).isEqualTo(id);
        Assertions.assertThat(updatedContent).isEqualTo("changedContent");
    }

    @Test
    void deleteTest() throws Exception {
        //given
        String contentAsString = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();;
        long id = objectMapper.readTree(contentAsString).get("serializedData").get("sheetPost").get("id").asLong();
        //when
        MvcResult mvcResult1 = mockMvc.perform(delete("/sheet/" + id)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        //then
        mockMvc.perform(get("/sheet/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    void findTest() throws Exception {
        //given
        String contentAsString = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();;
        Long id = objectMapper.readTree(contentAsString).get("serializedData").get("sheetPost").get("id").asLong();

        MvcResult mvcResult1 = mockMvc.perform(get("/sheet/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String contentAsString1 = mvcResult1.getResponse().getContentAsString();
        Long id1 = objectMapper.readTree(contentAsString1).get("serializedData").get("sheetPost").get("id").asLong();
        Assertions.assertThat(id).isEqualTo(id1);
    }

    @Test
    void findAllTest() throws Exception {
        //given
        String contentAsString1= mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();;
        Long id1 = objectMapper.readTree(contentAsString1).get("serializedData").get("sheetPost").get("id").asLong();
        build.setTitle("title2");
        MvcResult mvcResult2 = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String contentAsString2 = mvcResult2.getResponse().getContentAsString();
        long id2 = objectMapper.readTree(contentAsString2).get("serializedData").get("sheetPost").get("id").asLong();

        //when
        MvcResult result = mockMvc.perform(get("/sheet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        long id = objectMapper.readTree(contentAsString).get("serializedData").get("sheetPosts").get(0).get("id").asLong();
        Assertions.assertThat(id1).isEqualTo(id);
    }

    @Test
    @DisplayName("로그인한 유저는 댓글을 입력할 수 있다.")
    void commentTest() throws Exception {
        // given
        String contentAsString = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(contentAsString).get("serializedData").get("sheetPost").get("id").asLong();

        RegisterCommentDto dto = RegisterCommentDto.builder()
                .content("content")
                .build();
        String contentAsString1 = mockMvc.perform(post("/sheet/" + id + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        JsonNode jsonNode  = objectMapper.readTree(contentAsString1).get("serializedData").get("comments");
        List<CommentDto> commentList = new ArrayList<>();

        jsonNode.forEach(node -> {
            commentList.add(objectMapper.convertValue(node, CommentDto.class));
        });

        Assertions.assertThat(commentList).hasSize(1);
        Assertions.assertThat(commentList.get(0).getContent()).isEqualTo("content");
        User user1 = ((SecurityUser) commonUserService.loadUserByUsername("user1")).getUser();
        Assertions.assertThat(user1.getWroteComments()).hasSize(1);
        Assertions.assertThat(user1.getWroteComments().get(0).getContent()).isEqualTo("content");
    }

    @Test
    @DisplayName("댓글을 삭제할 수 있다.")
    void deleteCommentTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/sheet/write")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(build)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();

        String contentAsString = mvcResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(contentAsString).get("serializedData").get("sheetPost").get("id").asLong();

        RegisterCommentDto dto = RegisterCommentDto.builder()
                .content("content")
                .build();
        String contentAsString1 = mockMvc.perform(post("/sheet/" + id + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(contentAsString1).get("serializedData").get("comments");
        List<CommentDto> commentList = new ArrayList<>();

        jsonNode.forEach(node -> {
            commentList.add(objectMapper.convertValue(node, CommentDto.class));
        });
        String contentAsString2 = mockMvc.perform(delete("/sheet/" + id + "/comment/" + commentList.get(0).getId())
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        JsonNode jsonNode1  = objectMapper.readTree(contentAsString2).get("serializedData").get("comments");
        List<CommentDto> commentDtoList = new ArrayList<>();
        jsonNode1.forEach(element -> commentDtoList.add(objectMapper.convertValue(element, CommentDto.class)));

        Assertions.assertThat(commentDtoList).isEmpty();
        User user1 = ((SecurityUser) commonUserService.loadUserByUsername("user1")).getUser();
        Assertions.assertThat(user1.getWroteComments()).isEmpty();
    }


}