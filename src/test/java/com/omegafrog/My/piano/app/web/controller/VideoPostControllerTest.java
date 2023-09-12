package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.post.VideoPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostRegisterDto;
import jakarta.servlet.http.Cookie;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
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

    String accessToken;
    Cookie refreshToken;
    String wrongAccessToken;
    Cookie wrongRefreshToken;
    @Autowired
    private VideoPostRepository videoPostRepository;
    @Autowired
    private SecurityUserRepository securityUserRepository;

    @NoArgsConstructor
    @Data
    private static class LoginResult {
        private String status;
        private String message;
        private Map<String, String> serializedData;
    }

    @BeforeAll
    void getTokens() throws Exception, UsernameAlreadyExistException {
        commonUserService.registerUser(TestLoginUtil.user1);
        commonUserService.registerUser(TestLoginUtil.user2);
        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user1&password=password"))
                .andReturn();
        LoginResult loginResult = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), LoginResult.class);
        accessToken = loginResult.getSerializedData().get("access token");
        refreshToken = mvcResult.getResponse().getCookie("refreshToken");
        MvcResult mvcResult2 = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user2&password=password"))
                .andReturn();
        LoginResult loginResult2 = objectMapper
                .readValue(mvcResult2.getResponse().getContentAsString(), LoginResult.class);
        wrongAccessToken = loginResult2.getSerializedData().get("access token");
        wrongRefreshToken = mvcResult.getResponse().getCookie("refreshToken");
    }

    @Test
    @DisplayName("로그인한 유저는 video post를 추가할 수 있다.")
    void writeVideoPostTest() throws Exception {
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        String contentAsString = mockMvc.perform(post("/community/video-post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
        JsonNode jsonNode = objectMapper.readTree(text).get("videoPost");
        VideoPostDto videoPostDto = objectMapper.convertValue(jsonNode, VideoPostDto.class);
        Assertions.assertThat(videoPostDto.getTitle()).isEqualTo("title");
        SecurityUser user = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        Assertions.assertThat(user.getUser().getUploadedVideoPosts().get(0).getId()).isEqualTo(videoPostDto.getId());
    }

    @Test
    @DisplayName("로그인하지 않은 유저는 video post를 등록할 수 없다.")
    void writeVideoPostAuthorizationTest() throws Exception {
        VideoPostRegisterDto dto = VideoPostRegisterDto.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .build();
        mockMvc.perform(post("/community/video-post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("로그인한 유저는 자신의 video post를 수정할 수 있다.")
    void updateVideoPostTest() throws Exception {
        // given
        // video post 등록
        SecurityUser securityUser = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        User user = securityUser.getUser();
        VideoPost saved = videoPostRepository.save(VideoPost.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .author(user)
                .build());
        // when
        // video post update
        UpdateVideoPostDto dto = UpdateVideoPostDto
                .builder()
                .title("changed")
                .content("changedContent")
                .videoUrl("changedUrl")
                .build();
        String contentAsString = mockMvc.perform(post("/community/video-post/" + saved.getId())
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        // then
        String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
        JsonNode jsonNode = objectMapper.readTree(text).get("videoPost");
        VideoPostDto videoPostDto = objectMapper.convertValue(jsonNode, VideoPostDto.class);
        Assertions.assertThat(videoPostDto.getId()).isEqualTo(saved.getId());
        Assertions.assertThat(videoPostDto.getTitle()).isEqualTo("changed");
        Assertions.assertThat(videoPostDto.getContent()).isEqualTo("changedContent");
    }

    @Test
    @DisplayName("다른 유저의 videoPost를 수정할 수 없다.")
    void updateVideoPostAuthorizationTest() throws Exception {
        // given
        // video post 등록
        SecurityUser securityUser = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        User user = securityUser.getUser();
        VideoPost saved = videoPostRepository.save(VideoPost.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .author(user)
                .build());
        // when
        // video post update
        UpdateVideoPostDto dto = UpdateVideoPostDto
                .builder()
                .title("changed")
                .content("changedContent")
                .videoUrl("changedUrl")
                .build();
        mockMvc.perform(post("/community/video-post/" + saved.getId())
                        .header(HttpHeaders.AUTHORIZATION, wrongAccessToken)
                        .cookie(wrongRefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("로그인한 유저는 자신이 작성한 videoPost를 삭제할 수 있다.")
    void deleteVideoPostTest() throws Exception {
        // given
        // video post 등록
        SecurityUser securityUser = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        User user = securityUser.getUser();
        VideoPost saved = videoPostRepository.save(VideoPost.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .author(user)
                .build());
        //when
        mockMvc.perform(delete("/community/video-post/" + saved.getId())
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        Optional<VideoPost> byId = videoPostRepository.findById(saved.getId());
        securityUser = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        user = securityUser.getUser();
        //then
        Assertions.assertThat(byId).isEmpty();
        Assertions.assertThat(user.getUploadedVideoPosts()).isEmpty();
    }

    @Test
    @DisplayName("video post를 모두 조회할 수 있어야 한다.")
    void findAllVideoPostTest() throws Exception {
        //given
        SecurityUser securityUser = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        User user = securityUser.getUser();
        VideoPost saved = videoPostRepository.save(VideoPost.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .author(user)
                .build());
        SecurityUser securityUser2 = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user2.getUsername());
        User user2 = securityUser2.getUser();
        VideoPost saved2 = videoPostRepository.save(VideoPost.builder()
                .title("title2")
                .content("content2")
                .videoUrl("url")
                .author(user)
                .build());

        //when
        String contentAsString = mockMvc.perform(get("/community/video-post")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        List<VideoPostDto> videoPostDtos = new ArrayList<>();
        String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
        JsonNode jsonNode = objectMapper.readTree(text).get("videoPosts");
        jsonNode.forEach(node -> videoPostDtos.add(objectMapper.convertValue(node, VideoPostDto.class)));
        Assertions.assertThat(videoPostDtos).hasSize(2);
        Assertions.assertThat(videoPostDtos.get(0).getId()).isEqualTo(saved.getId());
        Assertions.assertThat(videoPostDtos.get(1).getId()).isEqualTo(saved2.getId());
    }

    @Test
    @DisplayName("로그인한 유저가 댓글을 달 수 있어야 한다.")
    void addCommentTest() throws Exception {
        //given
        SecurityUser securityUser = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        User user = securityUser.getUser();
        VideoPost saved = videoPostRepository.save(VideoPost.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .author(user)
                .build());

        //when
        RegisterCommentDto content = RegisterCommentDto.builder()
                .content("content")
                .build();

        String contentAsString = mockMvc.perform(post("/community/video-post/" + saved.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        //then
        String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
        JsonNode jsonNode = objectMapper.readTree(text).get("comments");
        List<CommentDto> commentDtos = new ArrayList<>();
        jsonNode.forEach(comment -> commentDtos.add(objectMapper.convertValue(comment, CommentDto.class)));
        Assertions.assertThat(commentDtos).hasSize(1);
        Assertions.assertThat(commentDtos.get(0).getContent()).isEqualTo("content");
        user = ((SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername())).getUser();
        Assertions.assertThat(user.getWroteComments()).hasSize(1);
        Assertions.assertThat(user.getWroteComments().get(0).getContent()).isEqualTo("content");
    }
    @Test
    @DisplayName("로그인하지 않은 유저는 댓글을 달 수 없다.")
    void addCommentAuthorizationTest() throws Exception{
        //given
        SecurityUser securityUser = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        User user = securityUser.getUser();
        VideoPost saved = videoPostRepository.save(VideoPost.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .author(user)
                .build());
        //when
        RegisterCommentDto content = RegisterCommentDto.builder()
                .content("content")
                .build();

        mockMvc.perform(post("/community/video-post/" + saved.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("작성자는 댓글을 삭제할 수 있다.")
    void deleteCommentTest() throws Exception{
        //given
        SecurityUser securityUser = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        User user = securityUser.getUser();
        VideoPost saved = videoPostRepository.save(VideoPost.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .author(user)
                .build());
        RegisterCommentDto content = RegisterCommentDto.builder()
                .content("content")
                .build();

        String contentAsString = mockMvc.perform(post("/community/video-post/" + saved.getId() + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();

        String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
        JsonNode jsonNode = objectMapper.readTree(text).get("comments");
        List<CommentDto> commentDtos = new ArrayList<>();
        jsonNode.forEach(comment -> commentDtos.add(objectMapper.convertValue(comment, CommentDto.class)));


        //when
        mockMvc.perform(delete("/community/video-post/" + saved.getId() + "/comment/" + commentDtos.get(0).getId())
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        user = ((SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername())).getUser();
        Assertions.assertThat(user.getWroteComments()).isEmpty();

    }

    @Test
    @DisplayName("video post에 달린 모든 댓글을 조회할 수 있다.")
    void getCommentsTest() throws Exception{
        SecurityUser securityUser = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        User user = securityUser.getUser();
        VideoPost saved = videoPostRepository.save(VideoPost.builder()
                .title("title")
                .content("content")
                .videoUrl("url")
                .author(user)
                .build());
        RegisterCommentDto content = RegisterCommentDto.builder()
                .content("content")
                .build();
        RegisterCommentDto content2 = RegisterCommentDto.builder()
                .content("content2")
                .build();

        mockMvc.perform(post("/community/video-post/" + saved.getId() + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
        mockMvc.perform(post("/community/video-post/" + saved.getId() + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content2))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));

        String contentAsString = mockMvc.perform(get("/community/video-post/" + saved.getId() + "/comments")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
                .andReturn().getResponse().getContentAsString();
        String text = objectMapper.readTree(contentAsString).get("serializedData").asText();
        JsonNode jsonNode = objectMapper.readTree(text).get("comments");
        List<CommentDto> comments = new ArrayList<>();
        jsonNode.forEach(node -> comments.add(objectMapper.convertValue(node, CommentDto.class)));

        Assertions.assertThat(comments).hasSize(2);
        Assertions.assertThat(comments.get(0).getContent()).isEqualTo("content");
        Assertions.assertThat(comments.get(1).getContent()).isEqualTo("content2");

        user = ((SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername())).getUser();
        Assertions.assertThat(user.getWroteComments()).hasSize(2);
        Assertions.assertThat(user.getWroteComments().get(0).getContent()).isEqualTo("content");
        Assertions.assertThat(user.getWroteComments().get(1).getContent()).isEqualTo("content2");

    }
    @AfterAll
    void getsdfas(){
        securityUserRepository.deleteAll();
    }
}
