package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.authorities.Authority;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.web.controller.PostController;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.PostApplicationService;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class PostControllerTest {

    private PostController controller;

    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PostApplicationService postApplicationService;



    @BeforeEach
    public void createController(){
        controller = new PostController(objectMapper, postApplicationService);

    }
    @Nested
    class LoggedInStateClass{

        private PostRegisterDto postDto;
        private User author;
        private Post entity;
        @BeforeEach
        public void setEntities(){
            postDto = PostRegisterDto.builder()
                    .title("title")
                    .content("content")
                    .build();
            author = User.builder()
                    .phoneNum(PhoneNum.builder()
                            .phoneNum("010-1111-2222")
                            .isAuthorized(true).build())
                    .profileSrc("src")
                    .cart(new Cart())
                    .build();
            ReflectionTestUtils.setField(author,"id",0L);

            entity = Post.builder()
                    .title(postDto.getTitle())
                    .content(postDto.getContent())
                    .author(author)
                    .build();
        }
        @BeforeEach
        public void setContextHolder(){
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(SecurityUser.builder()
                            .username("username")
                            .password("password")
                            .role(Role.USER)
                            .user(author)
                            .build(),
                            "password",
                            Arrays.asList(Authority.builder().authority(Role.USER.authorityName).build()))
            );
        }

        @Test
        @DisplayName("Post dto를 받아서 저장할 수 있어야 한다.")
        void writePost() throws JsonProcessingException {
            //given
            postDto = PostRegisterDto.builder()
                    .title("title")
                    .content("content")
                    .build();
            //when
            Post build = Post.builder()
                    .title(postDto.getTitle())
                    .content(postDto.getContent())
                    .author(author)
                    .build();
            ReflectionTestUtils.setField(build,"id", 0L);
            Mockito.when(postRepository.save(any(Post.class))).thenReturn(build);
            JsonAPIResponse apiResponse = controller.writePost( postDto);
            //then
            Assertions.assertThat(apiResponse).isNotNull();
            String serializedData = apiResponse.getSerializedData();
            JsonNode jsonNode = objectMapper.readTree(serializedData);
            String id = jsonNode.get("post").get("id").asText();
            Assertions.assertThat(id).isEqualTo("0");
        }
        @Test
        @DisplayName("post를 수정할 수 있어야 한다.")
        void updatePost() throws JsonProcessingException {
            //given
            Post build = Post.builder()
                    .title(postDto.getTitle())
                    .content(postDto.getContent())
                    .author(author)
                    .build();
            ReflectionTestUtils.setField(build,"id", 0L);

            Mockito.when(postRepository.findById(0L)).thenReturn(Optional.of(build));

            //when
            UpdatePostDto updateDto = UpdatePostDto.builder()
                    .title("updated")
                    .content("updatedContent")
                    .build();
            ReflectionTestUtils.setField(build, "title", updateDto.getTitle());
            ReflectionTestUtils.setField(build, "content", updateDto.getContent());

            Mockito.when(postRepository.save(build)).thenReturn(build);


            JsonAPIResponse response = controller.updatePost(0L, updateDto);


            Assertions.assertThat(response).isNotNull();
            JsonNode jsonNode = objectMapper.readTree(response.getSerializedData());
            String id = jsonNode.get("post").get("id").asText();
            Assertions.assertThat(id).isEqualTo("0");
            String updatedContent = jsonNode.get("post").get("content").asText();
            Assertions.assertThat(updatedContent).isEqualTo("updatedContent");
        }
        @Test
        @DisplayName("post를 삭제할 수 있다.")
        void deletePost() throws JsonProcessingException {
            //given
            Post build = Post.builder()
                    .title(postDto.getTitle())
                    .content(postDto.getContent())
                    .author(author)
                    .build();
            ReflectionTestUtils.setField(build,"id", 0L);

            Mockito.when(postRepository.findById(0L)).thenReturn(Optional.of(build));
            //when
            JsonAPIResponse response = controller.deletePost(0L);
            Assertions.assertThat(response).isNotNull();
            String status = response.getStatus();
            Assertions.assertThat(status).isEqualTo(HttpStatus.OK.toString());
        }
    }
    @Test
    @DisplayName("Post를 조회할 수 있다.")
    void findPost() throws JsonProcessingException {
        //given
        PostRegisterDto postDto = PostRegisterDto.builder()
                .title("title")
                .content("content")
                .build();
        User author = User.builder()
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-2222")
                        .isAuthorized(true).build())
                .profileSrc("src")
                .cart(new Cart())
                .build();
        ReflectionTestUtils.setField(author, "id", 0L);


        Post entity = Post.builder()
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .author(author)
                .build();
        ReflectionTestUtils.setField(entity, "id", 0L);
        Mockito.when(postRepository.findById(0L)).thenReturn(Optional.of(entity));
        //when
        JsonAPIResponse response = controller.findPost(0L);
        //then
        Assertions.assertThat(response).isNotNull();
        String data=response.getSerializedData();
        long id = objectMapper.readTree(data).get("post").get("id").asLong();
        String text = objectMapper.readTree(data).get("post").get("title").asText();
        Assertions.assertThat(id).isEqualTo(0L);
        Assertions.assertThat(text).isEqualTo("title");
    }
}