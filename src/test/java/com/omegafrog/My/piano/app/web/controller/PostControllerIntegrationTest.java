package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import com.omegafrog.My.piano.app.web.domain.article.Comment;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.web.dto.post.CommentDto;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CommonUserService commonUserService;

    @Autowired
    private ObjectMapper objectMapper;

    private static RegisterUserDto user1;
    private static RegisterUserDto user2;

    @Autowired
    private SecurityUserRepository securityUserRepository;
    private String accessToken;
    private Cookie refreshToken;

    /**
     * 로그인해서 accessToken과 refreshToken을 가져옴.
     * @throws Exception
     * @throws UsernameAlreadyExistException
     */
    @BeforeAll
    void getTokens() throws Exception, UsernameAlreadyExistException {
        SecurityContextHolder.clearContext();
        user1 = RegisterUserDto.builder()
                .name("user1")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .isAuthorized(false)
                        .build())
                .profileSrc("src")
                .loginMethod(LoginMethod.EMAIL)
                .email("email@email.com")
                .username("user1")
                .password("password")
                .build();
        user2 = RegisterUserDto.builder()
                .name("user2")
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .isAuthorized(false)
                        .build())
                .profileSrc("src")
                .email("email@email.com")
                .loginMethod(LoginMethod.EMAIL)
                .username("user2")
                .password("password")
                .build();
        SecurityUserDto saved1 = commonUserService.registerUser(user1);
        Optional<SecurityUser> byId = securityUserRepository.findById(saved1.getId());
        SecurityUserDto saved2 = commonUserService.registerUser(user2);
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
     void deleteusers(){
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
        String data = objectMapper.readTree(string).get("serializedData").asText();
        Long postId = objectMapper.readTree(data).get("post").get("id").asLong();
        System.out.println("postId = " + postId);
        //when
        MvcResult mvcResult2 = mockMvc.perform(get("/community/post/"+postId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andReturn();
        //then
        String contentAsString2 = mvcResult2.getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(contentAsString2);
        String serializedData = jsonNode.get("serializedData").asText();
        String id = objectMapper.readTree(serializedData).get("post").get("id").asText();
        String content = objectMapper.readTree(serializedData).get("post").get("content").asText();
        Assertions.assertThat(id).isEqualTo(postId.toString());
        Assertions.assertThat(content).isEqualTo("content");
    }
    @NoArgsConstructor
    @Data
    private static class LoginResult {
        private String status;
        private String message;
        private Map<String, String> serializedData;
    }

    @Test
    void updatePost() throws Exception {
        //given
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION,  accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String data = objectMapper.readTree(string).get("serializedData").asText();
        Long postId = objectMapper.readTree(data).get("post").get("id").asLong();
        System.out.println("postId = " + postId);

        UpdatePostDto updateDto = UpdatePostDto.builder()
                .title("changed")
                .content("changedContent")
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(post("/community/post/" + postId)
                        .header(HttpHeaders.AUTHORIZATION,  accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andReturn();
        //then
        String s = mvcResult.getResponse().getContentAsString();
        data = objectMapper.readTree(s).get("serializedData").asText();
        Long updatedId = objectMapper.readTree(data).get("post").get("id").asLong();
        String content = objectMapper.readTree(data).get("post").get("content").asText();
        Assertions.assertThat(updatedId).isEqualTo(postId);
        Assertions.assertThat(content).isEqualTo("changedContent");
    }
    @Test
    void addCommentTest() throws Exception {
        //given
        CommentDto commentDTO = CommentDto.builder()
                .content("test comment")
                .build();

        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        String string = mockMvc.perform(post("/community/post")
                        .header(HttpHeaders.AUTHORIZATION,  accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String data = objectMapper.readTree(string).get("serializedData").asText();
        Long postId = objectMapper.readTree(data).get("post").get("id").asLong();
        System.out.println("postId = " + postId);
        //when
        String contentAsString = mockMvc.perform(post("/community/post/" + postId + "/comment")
                        .header(HttpHeaders.AUTHORIZATION,  accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        //then
        data = objectMapper.readTree(contentAsString).get("serializedData").asText();
        System.out.println("data = " + data);

        JsonNode comments = objectMapper.readTree(data).get("comments");
        List<Comment> commentList = new ArrayList<>();
        for(JsonNode node : comments){
            commentList.add(objectMapper.readValue(node.toString(), Comment.class));
        }
        Assertions.assertThat(commentList.size()).isGreaterThan(0);
        Assertions.assertThat(commentList.get(0).getContent()).isEqualTo(commentDTO.getContent());
    }

    @Test
    @DisplayName("자신이 작성한 comment를 삭제할 수 있어야 한다.")
    void deleteCommentTest() throws Exception {
        //given
        CommentDto commentDTO = CommentDto.builder()
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
        String data = objectMapper.readTree(string).get("serializedData").asText();
        Long postId = objectMapper.readTree(data).get("post").get("id").asLong();
        System.out.println("postId = " + postId);

        data = mockMvc.perform(post("/community/post/" + postId + "/comment")
                        .header(HttpHeaders.AUTHORIZATION,  accessToken)
                        .cookie(refreshToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(commentDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        data = objectMapper.readTree(data).get("serializedData").asText();
        JsonNode comments = objectMapper.readTree(data).get("comments");
        Comment comment = objectMapper.readValue(comments.get(0).toString(), Comment.class);
        Long commentId = comment.getId();

        //when
        // 자신이 작성한 comment를 삭제함.
        String contentAsString = mockMvc.perform(delete("/community/post/" + postId + "/comment/" + commentId)
                        .header(HttpHeaders.AUTHORIZATION, accessToken)
                        .cookie(refreshToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode jsonNode = objectMapper.readTree(objectMapper.readTree(contentAsString).get("serializedData").asText())
                .get("comments");
        List<Comment> commentList = new ArrayList<>();
        for(JsonNode node : jsonNode) {
            commentList.add(objectMapper.readValue(node.toString(), Comment.class));
        }

        //then
        Optional<Comment> first = commentList.stream().filter(currentComment -> currentComment.getId().equals(commentId)).findFirst();
        Assertions.assertThat(first).isEmpty();
    }

    @Test
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
        String data = objectMapper.readTree(string).get("serializedData").asText();
        Long postId = objectMapper.readTree(data).get("post").get("id").asLong();
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
        Post post = objectMapper.readValue(objectMapper.readTree(objectMapper.readTree(contentAsString).get("serializedData").asText())
                .get("post").toString(),Post.class);
        Assertions.assertThat(post.getLikeCount()).isGreaterThan(0);
    }

    @Test
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
        String data = objectMapper.readTree(string).get("serializedData").asText();
        Long postId = objectMapper.readTree(data).get("post").get("id").asLong();
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
}