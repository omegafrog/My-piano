package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.Cleanup;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.RegisterLessonDto;
import com.omegafrog.My.piano.app.web.dto.lesson.UpdateLessonDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.RegisterSheetPostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import jakarta.persistence.EntityNotFoundException;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private SecurityUserRepository securityUserRepository;
    private TestUtil.TokenResponse artistToken;
    private TestUtil.TokenResponse otherToken;
    private TestUtil.TokenResponse otherToken2;
    @Autowired
    private TestUtil testUtil;

    @Autowired
    private Cleanup cleanUp;

    @BeforeEach
    void settings() throws Exception {
        // register lesson artist
        testUtil.register(mockMvc, TestUtil.user1);
        SecurityUser securityUser = securityUserRepository.findByUsername(TestUtil.user1.getUsername())
                .orElseThrow(() -> new EntityNotFoundException());
        securityUser.changeRole(Role.CREATOR);
        securityUserRepository.save(securityUser);

        artistToken = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());
        // register other user
        testUtil.register(mockMvc, TestUtil.user2);
        otherToken = testUtil.login(mockMvc, TestUtil.user2.getUsername(), TestUtil.user2.getPassword());

        testUtil.register(mockMvc, TestUtil.user3);
        otherToken2 = testUtil.login(mockMvc, TestUtil.user3.getUsername(), TestUtil.user3.getPassword());
    }

    @AfterEach
    void cleanUp() {
        cleanUp.cleanUp();
    }

    @Test
    @DisplayName("로그인하고 lesson을 생성할 수 있어야 한다.")
    void createLessonTest() throws Exception {
        //given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        //when
        String savedLessonString = mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerLessonDto))
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(savedLessonString).get("data").get("id").asLong();

        //then
        String contentAsString = mockMvc.perform(get("/api/v1/lessons"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(objectMapper.readTree(contentAsString).get("data").get(0).get("id").asLong())
                .isEqualTo(id);
    }

    @Test
    @DisplayName("로그인하지 않으면 lesson을 등록할 수 없다.")
    void createLessonAuthorizationTest() throws Exception {
        //given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        // when, then
        String body = objectMapper.writeValueAsString(registerLessonDto);
        mockMvc.perform(post("/api/v1/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인하고 lesson을 수정할 수 있어야 한다.")
    void updateLessonTest() throws Exception {
        //given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());
        LessonDto lessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);

        //when
        UpdateLessonDto updatedLessonDto = UpdateLessonDto.builder()
                .lessonInformation(lessonDto.getLessonInformation())
                .sheetId(lessonDto.getSheet().getId())
                .videoInformation(lessonDto.getVideoInformation())
                .price(30000)
                .title("changedTitle")
                .subTitle(lessonDto.getSubTitle())
                .build();
        String contentAsString = mockMvc.perform(post("/api/v1/lessons/" + lessonDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLessonDto))
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(contentAsString).get("data").get("id").asLong();

        //then
        mockMvc.perform(get("/api/v1/lessons/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("changedTitle"))
                .andDo(print());
    }

    @Test
    @DisplayName("작성자가 아닌 유저는 수정할 수 없다")
    void updateAuthorizationTest() throws Exception {
        // given
        // write lesson by artist
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        LessonDto lessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);

        // when
        // update lesson by other user
        UpdateLessonDto updatedLessonDto = UpdateLessonDto.builder()
                .lessonInformation(lessonDto.getLessonInformation())
                .sheetId(lessonDto.getSheet().getId())
                .videoInformation(lessonDto.getVideoInformation())
                .price(30000)
                .title("changedTitle")
                .subTitle(lessonDto.getSubTitle())
                .build();

        // 작성자가 아닌 유저는 접근할 수 없다.
        mockMvc.perform(post("/api/v1/lessons/" + lessonDto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedLessonDto))
                        .header(HttpHeaders.AUTHORIZATION, otherToken.getAccessToken())
                        .cookie(otherToken.getRefreshToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("작성자가 아닌 유저는 레슨을 삭제할 수 없다")
    void deleteAuthorizationTest() throws Exception {
        //given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        LessonDto lessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);
        // when, then
        // delete lesson by other user
        mockMvc.perform(delete("/api/v1/lessons/" + lessonDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, otherToken.getAccessToken())
                        .cookie(otherToken.getRefreshToken()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인하고 레슨을 삭제할 수 있다.")
    void deleteLessonTest() throws Exception {
        //given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        LessonDto lessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);

        //when, then
        // delete lesson by artist
        mockMvc.perform(delete("/api/v1/lessons/" + lessonDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, artistToken.getAccessToken())
                        .cookie(artistToken.getRefreshToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인하고 댓글을 작성할 수 있다.")
    void addCommentTest() throws Exception {
        // given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        LessonDto lessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);

        // when
        // write comment by other user
        RegisterCommentDto comment = RegisterCommentDto.builder()
                .content("comment")
                .build();

        String contentAsString = mockMvc.perform(post("/api/v1/lessons/" + lessonDto.getId() + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, otherToken.getAccessToken())
                        .cookie(otherToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        String content = objectMapper.readTree(contentAsString).get("data").get(0).get("content").asText();

        // then
        Assertions.assertThat(content).isEqualTo(comment.getContent());
    }

    @Test
    @DisplayName("로그인하지 않으면 댓글을 작성할 수 없다.")
    void addCommentAuthenticationTest() throws Exception {
        //given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        LessonDto lessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);

        // when
        // write comment without authorization header
        CommentDto comment = CommentDto.builder()
                .content("comment")
                .build();
        mockMvc.perform(post("/api/v1/lessons/" + lessonDto.getId() + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                // then
                // forbidden response
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    @DisplayName("자신이 쓴 댓글을 삭제할 수 있다.")
    void deleteCommentTest() throws Exception {
        // given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        LessonDto lessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);

        // write comment by other user
        RegisterCommentDto comment = RegisterCommentDto.builder()
                .content("comment")
                .build();

        String contentAsString = mockMvc.perform(post("/api/v1/lessons/" + lessonDto.getId() + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, otherToken.getAccessToken())
                        .cookie(otherToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        Long commentId = objectMapper.readTree(contentAsString).get("data").get(0).get("id").asLong();

        // when
        // delete comment by other user
        String contentAsString2 =
                mockMvc.perform(delete("/api/v1/lessons/" + lessonDto.getId() + "/comments/" + commentId)
                                .header(HttpHeaders.AUTHORIZATION, otherToken.getAccessToken())
                                .cookie(otherToken.getRefreshToken()))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                        .andReturn().getResponse().getContentAsString();

        // then
        // get all comments in lesson and it must be empty
        mockMvc.perform(get("/api/v1/lessons/" + lessonDto.getId() + "/comments"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content").isEmpty());
    }

    @Test
    @DisplayName("남의 댓글은 삭제할 수 없다.")
    void deleteCommentAuthorizationTest() throws Exception {
        //given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        LessonDto lessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);

        RegisterCommentDto comment = RegisterCommentDto.builder()
                .content("comment")
                .build();

        String contentAsString = mockMvc.perform(post("/api/v1/lessons/" + lessonDto.getId() + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, otherToken.getAccessToken())
                        .cookie(otherToken.getRefreshToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        ;
        Long commentId = objectMapper.readTree(contentAsString).get("data").get(0).get("id").asLong();

        // when
        // delete comment by other user 2 who does not wrote comment.
        mockMvc.perform(delete("/api/v1/lessons/" + lessonDto.getId() + "/comments/" + commentId)
                        .header(HttpHeaders.AUTHORIZATION, otherToken2.getAccessToken())
                        .cookie(otherToken2.getRefreshToken()))
                // then
                // get forbidden response
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    void findLessonTest() throws Exception {
        // given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        LessonDto savedLessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);

        // when
        // get lesson by Id
        String contentAsString = mockMvc.perform(get("/api/v1/lessons/" + savedLessonDto.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        // then
        // get lessonDto by response is equal to savedLessonDto
        LessonDto foundedLessonDto = objectMapper.convertValue(
                objectMapper.readTree(contentAsString).get("data"), LessonDto.class);

        Assertions.assertThat(savedLessonDto.getId()).isEqualTo(foundedLessonDto.getId());
        Assertions.assertThat(savedLessonDto.getTitle()).isEqualTo(foundedLessonDto.getTitle());
    }

    @Test
    @DisplayName("존재하지 않는 Lesson을 조회할 수 없다.")
    void findLessonFailedTest() throws Exception {
        //given
        RegisterSheetPostDto registerSheetPostDto = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        SheetPostDto sheetPostDto = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto);
        RegisterLessonDto registerLessonDto = TestUtil.registerLessonDto(sheetPostDto.getSheet().getId());

        LessonDto savedLessonDto = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto);
        Long notExisteId = 1L;
        while (savedLessonDto.getId().equals(notExisteId)) {
            notExisteId++;
        }
        // when
        // get lesson by not existed Id
        mockMvc.perform(get("/api/v1/lessons/" + notExisteId))
                // then
                // get 400 bad response
                .andExpect(status().is4xxClientError())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(print());
    }

    @Test
    void findAllLessonTest() throws Exception {
        // given
        // write lesson 1, 2
        RegisterSheetPostDto registerSheetPostDto1 = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto1);
        RegisterSheetPostDto registerSheetPostDto2 = TestUtil.registerSheetPostDto(TestUtil.registerSheetDto2);
        SheetPostDto sheetPostDto1 = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto1);
        SheetPostDto sheetPostDto2 = testUtil.writeSheetPost(mockMvc, artistToken, registerSheetPostDto2);
        RegisterLessonDto registerLessonDto1 = TestUtil.registerLessonDto(sheetPostDto1.getSheet().getId());
        RegisterLessonDto registerLessonDto2 = TestUtil.registerLessonDto(sheetPostDto2.getSheet().getId());

        LessonDto savedLessonDto1 = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto1);
        LessonDto savedLessonDto2 = testUtil.writeLesson(mockMvc, artistToken, registerLessonDto2);

        // when
        // get lessons
        String contentAsString = mockMvc.perform(get("/api/v1/lessons")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        // then
        // get list of lessonDto have savedLessons
        List<LessonDto> lessonDtoList = new ArrayList<>();
        Iterator<JsonNode> data = objectMapper.readTree(contentAsString).get("data").elements();
        while (data.hasNext()) {
            JsonNode next = data.next();
            lessonDtoList.add(objectMapper.convertValue(next, LessonDto.class));
        }

        Assertions.assertThat(lessonDtoList).size().isGreaterThan(1);
        Assertions.assertThat(lessonDtoList).extracting("id").contains(savedLessonDto1.getId(), savedLessonDto2.getId());
    }


}