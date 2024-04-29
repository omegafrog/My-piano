package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import jakarta.servlet.http.Cookie;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
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

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private CommonUserService commonUserService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SecurityUserRepository securityUserRepository;
    private String accessToken;
    private Cookie refreshToken;

    @NoArgsConstructor
    @Data
    private static class LoginResult {
        private int status;
        private String message;
        private Map<String, String> serializedData;
    }

    /**
     * 로그인해서 accessToken과 refreshToken을 가져옴.
     *
     * @throws Exception
     * @throws UsernameAlreadyExistException
     */
    @BeforeAll
    void getTokens() throws Exception, UsernameAlreadyExistException {

        commonUserService.registerUser(TestLoginUtil.user1);
        commonUserService.registerUser(TestLoginUtil.user2);
        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user1&password=password"))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(contentAsString, LoginResult.class);
        accessToken = loginResult.getSerializedData().get("access token");
        refreshToken = mvcResult.getResponse().getCookie("refreshToken");
    }

    /**
     * 유저를 모두 삭제함.
     */
    @AfterAll
    void deleteusers() {
        System.out.println("PostController : securityUserRepository.count() = " + securityUserRepository.count());
        List<SecurityUser> all = securityUserRepository.findAll();
        all.forEach(user -> System.out.println("user = " + user));
        securityUserRepository.deleteAll();
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("로그인한 유저는 커뮤니티 글을 작성하고 조회할 수 있어야 한다.")
    void writePost() throws Exception {
        //given
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        System.out.println("postId = " + postId);
        //when
        MvcResult mvcResult2 = mockMvc.perform(get("/community/post/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andReturn();
        //then
        String contentAsString2 = mvcResult2.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(contentAsString2);
        JsonNode post = jsonNode.get("serializedData").get("post");
        String id = post.get("id").asText();
        String content = post.get("content").asText();
        Assertions.assertThat(id).isEqualTo(postId.toString());
        Assertions.assertThat(content).isEqualTo("content");
    }

    @Test
    @DisplayName("로그인하지 않은 유저는 커뮤니티 글을 작성할 수 없다.")
    void writePostAuthorizationTest() throws Exception {
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        mockMvc.perform(post("/community/post")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }


    @Test
    @DisplayName("작성자는 자신이 작성할 커뮤니티 글을 수정할 수 있다.")
    void updatePost() throws Exception {
        //given
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        System.out.println("postId = " + postId);

        UpdatePostDto updateDto = UpdatePostDto.builder()
                .title("changed")
                .content("changedContent")
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/community/post/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andReturn();
        //then
        String s = mvcResult.getResponse().getContentAsString();
        JsonNode postDto1 =  objectMapper.readTree(s).get("serializedData").get("post");
        Long updatedId = postDto1.get("id").asLong();
        String content = postDto1.get("content").asText();
        Assertions.assertThat(updatedId).isEqualTo(postId);
        Assertions.assertThat(content).isEqualTo("changedContent");
    }

    @Test
    @DisplayName("작성자가 아닌 유저는 커뮤니티 글을 수정할 수 없다.")
    void updatePostAuthorizationTest() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user2&password=password"))
                .andReturn();
        String contentAsString = mvcResult.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(contentAsString, LoginResult.class);
        String wrongAccessToken = loginResult.getSerializedData().get("access token");
        Cookie wrongRefreshToken = mvcResult.getResponse().getCookie("refreshToken");
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        System.out.println("postId = " + postId);

        UpdatePostDto updateDto = UpdatePostDto.builder()
                .title("changed")
                .content("changedContent")
                .build();

        //when
        mockMvc.perform(post("/community/post/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, wrongAccessToken)
                        .cookie(wrongRefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("post에 comment를 추가할 수 있어야 한다.")
    void addCommentTest() throws Exception {
        //given
        RegisterCommentDto commentDTO = RegisterCommentDto.builder()
                .content("test comment")
                .build();

        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        System.out.println("postId = " + postId);
        //when
        String contentAsString = mockMvc.perform(post("/community/post/" + postId + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        //then
        JsonNode comments = objectMapper.readTree(contentAsString).get("serializedData").get("comments");

        List<CommentDto> commentList = new ArrayList<>();
        for (JsonNode node : comments) {
            commentList.add(objectMapper.convertValue(node, CommentDto.class));
        }
        Assertions.assertThat(commentList.size()).isGreaterThan(0);
        Assertions.assertThat(commentList.get(0).getContent()).isEqualTo(commentDTO.getContent());

        SecurityUser user = (SecurityUser) commonUserService.loadUserByUsername(TestLoginUtil.user1.getUsername());
        Assertions.assertThat(user.getUser().getWroteComments()).hasSize(1);
        Assertions.assertThat(user.getUser().getWroteComments().get(0).getId()).isEqualTo(commentList.get(0).getId());
    }

    @Test
    @DisplayName("로그인하지 않으면 comment를 작성할 수 없다.")
    @Transactional
    void addCommentAuthorizationTest() throws Exception {
        //given
        RegisterCommentDto commentDTO = RegisterCommentDto.builder()
                .content("test comment")
                .build();

        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        System.out.println("postId = " + postId);
        //when
        mockMvc.perform(post("/community/post/" + postId + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @Transactional
    @DisplayName("자신이 작성한 comment를 삭제할 수 있어야 한다.")
    void deleteCommentTest() throws Exception {
        //given
        RegisterCommentDto commentDTO = RegisterCommentDto.builder()
                .content("test comment")
                .build();

        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        // post 등록
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        System.out.println("postId = " + postId);
        // post에 comment 등록
        String contentAsString1 = mockMvc.perform(post("/community/post/" + postId + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode comments = objectMapper.readTree(contentAsString1).get("serializedData").get("comments");
        CommentDto comment = objectMapper.convertValue(comments.get(0), CommentDto.class);
        Long commentId = comment.getId();

        //when
        // 자신이 작성한 comment를 삭제함.
        String contentAsString = mockMvc.perform(delete("/community/post/" + postId + "/comment/" + commentId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(contentAsString).get("serializedData").get("comments");
        List<Comment> commentList = new ArrayList<>();
        for (JsonNode node : jsonNode) {
            commentList.add(objectMapper.readValue(node.toString(), Comment.class));
        }

        //then
        Optional<Comment> first = commentList.stream().filter(currentComment -> currentComment.getId().equals(commentId)).findFirst();
        Assertions.assertThat(first).isEmpty();
    }

    @Test
    @Transactional
    @DisplayName("작성자가 아닌 유저는 댓글을 삭제할 수 없다.")
    void deleteCommentAuthorizationTest() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user2&password=password"))
                .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(contentAsString, LoginResult.class);
        String wrongAccessToken = loginResult.getSerializedData().get("access token");
        Cookie wrongRefreshToken = result.getResponse().getCookie("refreshToken");
        //given
        RegisterCommentDto commentDTO = RegisterCommentDto.builder()
                .content("test comment")
                .build();

        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        // post 등록
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        // post에 comment 등록
        String contentAsString1 = mockMvc.perform(post("/community/post/" + postId + "/comment")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode comments = objectMapper.readTree(contentAsString1).get("serializedData").get("comments");
        CommentDto comment = objectMapper.readValue(comments.get(0).toString(), CommentDto.class);
        Long commentId = comment.getId();
        //when
        // 다른 사람이 작성한 comment를 삭제함.
        mockMvc.perform(delete("/community/post/" + postId + "/comment/" + commentId)
                        .header(HttpHeaders.AUTHORIZATION, wrongAccessToken)
                        .cookie(wrongRefreshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }

    @Test
    @DisplayName("커뮤니티 글에 좋아요를 누를 수 있다.")
    void likePostTest() throws Exception {
        //given
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        //when
        mockMvc.perform(get("/community/post/" + postId + "/like")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk());

        //then
        String contentAsString = mockMvc.perform(get("/community/post/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        int likeCount = objectMapper.readTree(contentAsString).get("serializedData").get("post").get("likeCount").asInt();

        Assertions.assertThat(likeCount).isGreaterThan(0);
    }

    @Test
    @DisplayName("커뮤니티 글을 삭제할 수 있다.")
    @Transactional
    void deletePost() throws Exception {
        //given
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        //when
        String s = mockMvc.perform(delete("/community/post/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        //then
        String message = objectMapper.readTree(s).get("message").asText();
        Assertions.assertThat(message).isEqualTo("delete post success");
    }
    @Test
    @DisplayName("작성자가 아닌 유저는 커뮤니티 글을 삭제할 수 없다.")
    @Transactional
    void deletePostAuthorizationTest() throws Exception {
        MvcResult result = mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .content("username=user2&password=password"))
                .andReturn();
        String contentAsString = result.getResponse().getContentAsString();
        LoginResult loginResult = objectMapper.readValue(contentAsString, LoginResult.class);
        String wrongAccessToken = loginResult.getSerializedData().get("access token");
        Cookie wrongRefreshToken = result.getResponse().getCookie("refreshToken");
        //given
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Long postId = objectMapper.readTree(string).get("serializedData").get("post").get("id").asLong();
        //when
        mockMvc.perform(delete("/community/post/" + postId)
                        .header(HttpHeaders.AUTHORIZATION, wrongAccessToken)
                        .cookie(wrongRefreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(HttpStatus.FORBIDDEN.value()));
    }
}