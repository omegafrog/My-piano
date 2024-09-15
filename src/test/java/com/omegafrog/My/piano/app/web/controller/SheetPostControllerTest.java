package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.Cleanup;
import com.omegafrog.My.piano.app.TestUtil;
import com.omegafrog.My.piano.app.TestUtilConfig;
import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.UpdateSheetPostDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@SpringBootTest
@Import(TestUtilConfig.class)
class SheetPostControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private Cleanup cleanup;

    @Autowired
    private TestUtil testUtil;


    @BeforeEach
    void cleanUp() {
        cleanup.cleanUp();
    }

    @Test
    void saveTest() throws Exception {
        testUtil.register(mockMvc, TestUtil.user1);
        testUtil.register(mockMvc, TestUtil.user2);
        TestUtil.TokenResponse artistToken = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());
        TestUtil.TokenResponse userToken = testUtil.login(mockMvc, TestUtil.user2.getUsername(), TestUtil.user2.getPassword());
        RegisterSheetPostDto registerDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        MockMultipartFile file = new MockMultipartFile("sheetFiles", "img.pdf", "application/pdf",
                new FileInputStream("src/test/sheet.pdf"));
        String contentAsString = mockMvc.perform(multipart("/api/v1/sheet-post")
                        .file(file)
                        .part(new MockPart("sheetInfo", objectMapper.writeValueAsString(registerDto).getBytes(
                                StandardCharsets.UTF_8
                        )))
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        String content = objectMapper.readTree(contentAsString).get("data").get("content").asText();
        Assertions.assertThat(content).isEqualTo("content");
    }

    @Test
    void updateTest() throws Exception {
//            given
        testUtil.register(mockMvc, TestUtil.user1);
        testUtil.register(mockMvc, TestUtil.user2);
        TestUtil.TokenResponse artistToken = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());
        TestUtil.TokenResponse userToken = testUtil.login(mockMvc, TestUtil.user2.getUsername(), TestUtil.user2.getPassword());
        RegisterSheetPostDto registerDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        MockMultipartFile file = new MockMultipartFile("sheetFiles", "img.pdf", "application/pdf",
                new FileInputStream("src/test/sheet.pdf"));
        String contentAsString = mockMvc.perform(multipart("/api/v1/sheet-post")
                        .file(file)
                        .part(new MockPart("sheetInfo", objectMapper.writeValueAsString(registerDto).getBytes(
                                StandardCharsets.UTF_8
                        )))
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(contentAsString).get("data").get("id").asLong();

        //when
        MockMultipartFile file1 = new MockMultipartFile("file", (byte[]) null);
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
        MvcResult mvcResult1 = mockMvc.perform(multipart("/api/v1/sheet-post/" + id)
                        .file(file1)
                        .part(new MockPart("dto", objectMapper.writeValueAsString(updateBuild).getBytes()))
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateBuild)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String contentAsString1 = mvcResult1.getResponse().getContentAsString();
        JsonNode sheetPost = objectMapper.readTree(contentAsString1).get("data");
        String updatedContent = sheetPost.get("content").asText();
        long updatedId = sheetPost.get("id").asLong();
        Assertions.assertThat(updatedId).isEqualTo(id);
        Assertions.assertThat(updatedContent).isEqualTo("changedContent");
    }

    @Test
    void deleteTest() throws Exception {
        //given
        testUtil.register(mockMvc, TestUtil.user1);
        TestUtil.TokenResponse artistToken = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());
        testUtil.register(mockMvc, TestUtil.user2);
        TestUtil.TokenResponse userToken = testUtil.login(mockMvc, TestUtil.user2.getUsername(), TestUtil.user2.getPassword());
        RegisterSheetPostDto registerDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerDto);
        Long id = sheetPostDto.getId();
        //when
        MvcResult mvcResult1 = mockMvc.perform(delete("/api/v1/sheet-post/" + id)
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        //then
        mockMvc.perform(get("/api/v1/sheet-post/" + id))
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    void findTest() throws Exception {
        //given
        testUtil.register(mockMvc, TestUtil.user1);
        TestUtil.TokenResponse artistToken = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());
        testUtil.register(mockMvc, TestUtil.user2);
        TestUtil.TokenResponse userToken = testUtil.login(mockMvc, TestUtil.user2.getUsername(), TestUtil.user2.getPassword());
        RegisterSheetPostDto registerDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerDto);
        Long id = sheetPostDto.getId();

        MvcResult mvcResult1 = mockMvc.perform(get("/api/v1/sheet-post/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn();
        String contentAsString1 = mvcResult1.getResponse().getContentAsString();
        Long id1 = objectMapper.readTree(contentAsString1).get("data").get("id").asLong();
        Assertions.assertThat(id).isEqualTo(id1);
    }

    @Test
    void findAllTest() throws Exception {
        //given
        testUtil.register(mockMvc, TestUtil.user1);
        TestUtil.TokenResponse artistToken = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());
        testUtil.register(mockMvc, TestUtil.user2);
        TestUtil.TokenResponse userToken = testUtil.login(mockMvc, TestUtil.user2.getUsername(), TestUtil.user2.getPassword());
        RegisterSheetPostDto registerDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        MockMultipartFile file = new MockMultipartFile("sheetFiles", "img.pdf", "application/pdf",
                new FileInputStream("src/test/sheet.pdf"));
        String contentAsString = mockMvc.perform(multipart("/api/v1/sheet-post")
                        .file(file)
                        .part(new MockPart("sheetInfo", objectMapper.writeValueAsString(registerDto).getBytes(
                                StandardCharsets.UTF_8
                        )))
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readTree(contentAsString).get("data").get("id").asLong();

        RegisterSheetPostDto registerDto2 = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto2);
        MockMultipartFile file2 = new MockMultipartFile("sheetFiles", "img.pdf", "application/pdf",
                new FileInputStream("src/test/sheet.pdf"));
        String contentAsString2 = mockMvc.perform(multipart("/api/v1/sheet-post")
                        .file(file2)
                        .part(new MockPart("sheetInfo", objectMapper.writeValueAsString(registerDto2).getBytes(
                                StandardCharsets.UTF_8
                        )))
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        Long id2 = objectMapper.readTree(contentAsString2).get("data").get("id").asLong();

        //when
        String sheetposts = mockMvc.perform(get("/api/v1/sheet-post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        Iterator<JsonNode> data = objectMapper.readTree(sheetposts).get("data").get("content").elements();
        List<SheetPostDto> sheetPostDtos = new ArrayList<>();
        while (data.hasNext())
            sheetPostDtos.add(objectMapper.convertValue(data.next(), SheetPostDto.class));

        Assertions.assertThat(sheetPostDtos).extracting("id").containsAnyOf(id, id2);
    }

    @Test
    @DisplayName("로그인한 유저는 댓글을 입력할 수 있다.")
    void commentTest() throws Exception {
        // given
        testUtil.register(mockMvc, TestUtil.user1);
        TestUtil.TokenResponse artistToken = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());
        testUtil.register(mockMvc, TestUtil.user2);
        TestUtil.TokenResponse userToken = testUtil.login(mockMvc, TestUtil.user2.getUsername(), TestUtil.user2.getPassword());
        RegisterSheetPostDto registerDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        MockMultipartFile file = new MockMultipartFile("sheetFiles", "img.pdf", "application/pdf",
                new FileInputStream("src/test/sheet.pdf"));
        String contentAsString = mockMvc.perform(multipart("/api/v1/sheet-post")
                        .file(file)
                        .part(new MockPart("sheetInfo", objectMapper.writeValueAsString(registerDto).getBytes(
                                StandardCharsets.UTF_8
                        )))
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readTree(contentAsString).get("data").get("id").asLong();

        RegisterCommentDto dto = RegisterCommentDto.builder()
                .content("content")
                .build();
        String contentAsString1 = mockMvc.perform(post("/api/v1/sheet-post/" + id + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, userToken.getAccessToken())
                        .cookie(userToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(contentAsString1).get("data");
        List<CommentDto> commentList = new ArrayList<>();

        jsonNode.forEach(node -> {
            commentList.add(objectMapper.convertValue(node, CommentDto.class));
        });

        Assertions.assertThat(commentList).hasSize(1);
        Assertions.assertThat(commentList.get(0).getContent()).isEqualTo("content");
//        User user1 = ((SecurityUser) commonUserService.loadUserByUsername("user1")).getUser();
//        Assertions.assertThat(user1.getWroteComments()).hasSize(1);
//        Assertions.assertThat(user1.getWroteComments().get(0).getContent()).isEqualTo("content");
    }

    @Test
    @DisplayName("댓글을 삭제할 수 있다.")
    void deleteCommentTest() throws Exception {
        testUtil.register(mockMvc, TestUtil.user1);
        TestUtil.TokenResponse artistToken = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());
        testUtil.register(mockMvc, TestUtil.user2);
        TestUtil.TokenResponse userToken = testUtil.login(mockMvc, TestUtil.user2.getUsername(), TestUtil.user2.getPassword());
        RegisterSheetPostDto registerDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerDto);

        RegisterCommentDto dto = RegisterCommentDto.builder()
                .content("content")
                .build();
        String contentAsString1 = mockMvc.perform(post("/api/v1/sheet-post/" + sheetPostDto.getId() + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, userToken.getAccessToken())
                        .cookie(userToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(contentAsString1).get("data");
        List<CommentDto> commentList = new ArrayList<>();

        jsonNode.forEach(node -> {
            commentList.add(objectMapper.convertValue(node, CommentDto.class));
        });
        String contentAsString2 = mockMvc.perform(delete("/api/v1/sheet-post/" + sheetPostDto.getId() + "/comments/" + commentList.get(0).getId())
                        .header(HttpHeaders.AUTHORIZATION, userToken.getAccessToken())
                        .cookie(userToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        JsonNode jsonNode1 = objectMapper.readTree(contentAsString2).get("data");
        List<CommentDto> commentDtoList = new ArrayList<>();
        jsonNode1.forEach(element -> commentDtoList.add(objectMapper.convertValue(element, CommentDto.class)));

        Assertions.assertThat(commentDtoList).isEmpty();
        String comments = mockMvc.perform(get("/api/v1/user/comments")
                        .header(HttpHeaders.AUTHORIZATION, userToken.getAccessToken())
                        .cookie(userToken.getRefreshToken()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        Iterator<JsonNode> data = objectMapper.readTree(comments).get("data").elements();
        List<CommentDto> userComments = new ArrayList<>();

        while (data.hasNext())
            userComments.add(objectMapper.convertValue(data.next(), CommentDto.class));

        Assertions.assertThat(userComments).isEmpty();
    }
}