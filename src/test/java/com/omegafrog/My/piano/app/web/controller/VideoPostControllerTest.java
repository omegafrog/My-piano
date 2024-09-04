package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.Cleanup;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.post.VideoPostRepository;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostRegisterDto;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@RequiredArgsConstructor
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VideoPostControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private CommonUserService commonUserService;

    String user1AccessToken;
    Cookie user1RefreshToken;
    String user2user1AccessToken;
    Cookie user2user1RefreshToken;

    @Autowired
    private VideoPostRepository videoPostRepository;
    @Autowired
    private Cleanup cleanup;

    @AfterEach
    void cleanUp() {
        cleanup.cleanUp();
    }

    @BeforeEach
    public void loginNRegister() throws Exception {
        register(TestUtil.user1);
        register(TestUtil.user2);

        MvcResult user1Result = login(TestUtil.user1.getUsername(), TestUtil.user1.getPassword());

        user1AccessToken = objectMapper.readTree(user1Result.getResponse().getContentAsString())
                .get("data").get("access token").asText();
        user1RefreshToken = user1Result.getResponse().getCookie("refreshToken");

        MvcResult user2Result = login(TestUtil.user2.getUsername(), TestUtil.user2.getPassword());

        user2user1AccessToken = objectMapper.readTree(user2Result.getResponse().getContentAsString())
                .get("data").get("access token").asText();
        user2user1RefreshToken = user2Result.getResponse().getCookie("refreshToken");
    }

    private MvcResult login(String username, String password) throws Exception {
        String content = "username=" + username + "&password=" + password;
        return mockMvc.perform(post("/api/v1/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andDo(print())
                .andReturn();
    }

    private void register(RegisterUserDto user1) throws Exception {
        String s = objectMapper.writeValueAsString(user1);
        MockMultipartFile registerInfo = new MockMultipartFile("registerInfo", "", "application/json",
                s.getBytes());
        mockMvc.perform(multipart("/api/v1/user/register")
                        .file(registerInfo)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    @DisplayName("로그인한 유저는 video post를 추가할 수 있다.")
    void writeVideoPostTest() throws Exception {
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String videoPost1 = mockMvc.perform(post("/api/v1/video-post")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        String title = objectMapper.readTree(videoPost1).get("data").get("title").asText();
        Long id = objectMapper.readTree(videoPost1).get("data").get("id").asLong();

        Assertions.assertThat(title.equals("title"));

        String responseBody = mockMvc.perform(get("/api/v1/user/uploaded-video-posts")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(responseBody).get("data").get("content");

        Assertions.assertThat(node.get(0).get("id").asLong()).isEqualTo(id);
    }

    @Test
    @DisplayName("로그인하지 않은 유저는 video post를 등록할 수 없다.")
    void writeVideoPostAuthorizationTest() throws Exception {
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        mockMvc.perform(post("/api/v1/video-post")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
    }

    @Test
    @DisplayName("로그인한 유저는 자신의 video post를 수정할 수 있다.")
    void updateVideoPostTest() throws Exception {
        // given
        // video post 등록
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String contentAsString = mockMvc.perform(post("/api/v1/video-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(contentAsString).get("data").get("id").asLong();

        // when
        // video post update
        UpdateVideoPostDto updateVideoDto = UpdateVideoPostDto
                .builder()
                .title("changed")
                .content("changedContent")
                .videoUrl("changedUrl")
                .build();
        String changedVideoPost = mockMvc.perform(post("/api/v1/video-post/" + id)
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateVideoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        // then
        JsonNode jsonNode = objectMapper.readTree(changedVideoPost).get("data");
        VideoPostDto videoPostDto = objectMapper.convertValue(jsonNode, VideoPostDto.class);
        Assertions.assertThat(videoPostDto.getId()).isEqualTo(id);
        Assertions.assertThat(videoPostDto.getTitle()).isEqualTo("changed");
        Assertions.assertThat(videoPostDto.getContent()).isEqualTo("changedContent");
    }

    @Test
    @DisplayName("다른 유저의 videoPost를 수정할 수 없다.")
    void updateVideoPostAuthorizationTest() throws Exception {
        // given
        // video post 등록
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String contentAsString = mockMvc.perform(post("/api/v1/video-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(contentAsString).get("data").get("id").asLong();
        // when
        // video post update
        mockMvc.perform(post("/api/v1/video-post/" + id)
                        .header(HttpHeaders.AUTHORIZATION, user2user1AccessToken)
                        .cookie(user2user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("로그인한 유저는 자신이 작성한 videoPost를 삭제할 수 있다.")
    void deleteVideoPostTest() throws Exception {
        // given
        // video post 등록
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String contentAsString = mockMvc.perform(post("/api/v1/video-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(contentAsString).get("data").get("id").asLong();
        //when
        mockMvc.perform(delete("/api/v1/video-post/" + id)
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));

        String responseBody = mockMvc.perform(get("/api/v1/user/uploaded-video-posts")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andReturn().getResponse().getContentAsString();

        JsonNode node = objectMapper.readTree(responseBody)
                .get("data").get("content");


        Optional<VideoPost> byId = videoPostRepository.findById(id);

        //then
        Assertions.assertThat(byId).isEmpty();
        Assertions.assertThat(node).isEmpty();
    }

    @Test
    @DisplayName("video post를 모두 조회할 수 있어야 한다.")
    void findAllVideoPostTest() throws Exception {
        //given
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String contentAsString = mockMvc.perform(post("/api/v1/video-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        long id1 = objectMapper.readTree(contentAsString).get("data").get("id").asLong();


        VideoPostRegisterDto dto2 = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String contentAsString2 = mockMvc.perform(post("/api/v1/video-post")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        long id2 = objectMapper.readTree(contentAsString2).get("data").get("id").asLong();
        //when
        String comments1 = mockMvc.perform(get("/api/v1/video-post")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        List<VideoPostDto> videoPostDtos = new ArrayList<>();
        JsonNode jsonNode = objectMapper.readTree(comments1).get("data");
        jsonNode.forEach(node -> videoPostDtos.add(objectMapper.convertValue(node, VideoPostDto.class)));
        Assertions.assertThat(videoPostDtos).hasSize(2);
        Assertions.assertThat(videoPostDtos.get(0).getId()).isEqualTo(id1);
        Assertions.assertThat(videoPostDtos.get(1).getId()).isEqualTo(id2);
    }

    @Test
    @DisplayName("로그인한 유저가 댓글을 달 수 있어야 한다.")
    void addCommentTest() throws Exception {
        //given
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String contentAsString = mockMvc.perform(post("/api/v1/video-post")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(contentAsString).get("data").get("id").asLong();

        //when
        RegisterCommentDto content = RegisterCommentDto.builder()
                .content("content")
                .build();

        String comments1 = mockMvc.perform(post("/api/v1/video-post/" + id + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        //then
        JsonNode jsonNode = objectMapper.readTree(comments1).get("data");
        List<CommentDto> commentDtos = new ArrayList<>();
        jsonNode.forEach(comment -> commentDtos.add(objectMapper.convertValue(comment, CommentDto.class)));
        Assertions.assertThat(commentDtos).hasSize(1);
        Assertions.assertThat(commentDtos.get(0).getContent()).isEqualTo("content");

        String bodyString = mockMvc.perform(get("/api/v1/user/comments")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        JsonNode wroteComments = objectMapper.readTree(bodyString).get("data");

        Assertions.assertThat(wroteComments).hasSize(1);
        Assertions.assertThat(wroteComments.get(0).get("content").asText()).isEqualTo("content");
    }

    @Test
    @DisplayName("로그인하지 않은 유저는 댓글을 달 수 없다.")
    void addCommentAuthorizationTest() throws Exception {
        //given
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String contentAsString = mockMvc.perform(post("/api/v1/video-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        long id = objectMapper.readTree(contentAsString).get("data").get("id").asLong();

        //when
        RegisterCommentDto content = RegisterCommentDto.builder()
                .content("content")
                .build();

        mockMvc.perform(post("/api/v1/video-post/" + id + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
                )
                .andExpect(status().isForbidden())
                .andDo(print());
    }

    @Test
    @DisplayName("작성자는 댓글을 삭제할 수 있다.")
    void deleteCommentTest() throws Exception {
        //given
        // write videoPost
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String comments1 = mockMvc.perform(post("/api/v1/video-post")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(comments1).get("data").get("id").asLong();

        // write comment in this video post
        String s2 = mockMvc.perform(post("/api/v1/video-post/" + id + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(RegisterCommentDto.builder().content("hi").build()))
                )
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(s2).get("data");
        List<CommentDto> commentDtos = new ArrayList<>();
        jsonNode.forEach(comment -> commentDtos.add(objectMapper.convertValue(comment, CommentDto.class)));


        //when
        mockMvc.perform(delete("/api/v1/video-post/" + id + "/comments/" + commentDtos.get(0).getId())
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        String comments = mockMvc.perform(get("/api/v1/video-post/" + id + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        JsonNode commentNodes = objectMapper.readTree(comments).get("data").get("content");

        Assertions.assertThat(commentNodes).isEmpty();
    }

    @Test
    @DisplayName("video post에 달린 모든 댓글을 조회할 수 있다.")
    void getCommentsTest() throws Exception {
        //given
        // write videoPost
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String comments1 = mockMvc.perform(post("/api/v1/video-post")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(comments1).get("data").get("id").asLong();

        //write comments
        RegisterCommentDto content = RegisterCommentDto.builder()
                .content("content")
                .build();
        RegisterCommentDto content2 = RegisterCommentDto.builder()
                .content("content2")
                .build();

        mockMvc.perform(post("/api/v1/video-post/" + id + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        mockMvc.perform(post("/api/v1/video-post/" + id + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));

        //when
        //get videoPost's comments
        String comments = mockMvc.perform(get("/api/v1/video-post/" + id + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(comments).get("data").get("content");
        List<CommentDto> commentList = new ArrayList<>();
        jsonNode.forEach(node -> commentList.add(objectMapper.convertValue(node, CommentDto.class)));

        Assertions.assertThat(commentList).hasSize(2);
        Assertions.assertThat(commentList).extracting("content").contains("content", "content2");

        // 작성한 유저가 2개의 댓글을 가지고 있는지
        String bodyString = mockMvc.perform(get("/api/v1/user/comments")
                        .header(HttpHeaders.AUTHORIZATION, user1AccessToken)
                        .cookie(user1RefreshToken))
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        JsonNode wroteComments = objectMapper.readTree(bodyString).get("data");

        Assertions.assertThat(wroteComments).hasSize(2);
        Assertions.assertThat(wroteComments.get(0).get("content").asText()).containsAnyOf("content", "content2");

    }

}
