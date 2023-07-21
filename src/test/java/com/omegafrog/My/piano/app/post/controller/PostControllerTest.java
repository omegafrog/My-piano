package com.omegafrog.My.piano.app.post.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
class PostControllerTest {

    private static class TestPostRepository implements PostRepository {

        private static Map<Long, Post> storage = new ConcurrentHashMap<>();
        private Long sequence = 0L;

        public Map getStorage(){
            return storage;
        }

        @Override
        public Post save(Post post) {
            if(post.getId()!=null){
                storage.remove(post.getId());
                storage.put(post.getId(),post);
            }else {
                storage.put(sequence++, post);
                ReflectionTestUtils.setField(post, "id", sequence - 1);
            }
            return post;
        }

        @Override
        public Optional<Post> findById(Long id) {
            return Optional.ofNullable(storage.get(id));
        }

        @Override
        public void deleteById(Long id) {
            storage.remove(id);
        }

        @Override
        public void deleteAll() {
            storage.clear();
        }
    }
    private PostController controller;
    private final PostRepository postRepository = new TestPostRepository();

    private  ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @BeforeEach
    public void createController(){
        PostApplicationService postApplicationService = Mockito.mock(PostApplicationService.class);
        UserRepository userRepository = Mockito.mock(UserRepository.class);
        controller = new PostController( objectMapper, postApplicationService);
    }
    @AfterEach
    public void clearRepository(){
        TestPostRepository postRepository1 = (TestPostRepository) postRepository;
        postRepository1.getStorage().clear();

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
            Authentication authentication = Mockito.mock(Authentication.class);
            when(authentication.getDetails()).thenReturn(author);
            SecurityContext securityContext = Mockito.mock(SecurityContext.class);
            Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
            SecurityContextHolder.setContext(securityContext);
        }
        @AfterEach
        public void clearRepository(){
            TestPostRepository postRepository1 = (TestPostRepository) postRepository;
            postRepository1.getStorage().clear();
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
            JsonAPIResponse apiResponse = controller.writePost(SecurityContextHolder.getContext().getAuthentication(), postDto);
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
            Post saved = postRepository.save(entity);
            UpdatePostDto updateDto = UpdatePostDto.builder()
                    .title("updated")
                    .content("updatedContent")
                    .build();
            JsonAPIResponse response = controller.updatePost(SecurityContextHolder.getContext().getAuthentication(),0L, updateDto);
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
            Post saved = postRepository.save(entity);
            //when
            JsonAPIResponse response = controller.deletePost(SecurityContextHolder.getContext().getAuthentication(),saved.getId());
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

        Post entity = Post.builder()
                .title(postDto.getTitle())
                .content(postDto.getContent())
                .author(author)
                .build();
        Post saved = postRepository.save(entity);
        //when
        JsonAPIResponse response = controller.findPost(0L);
        //then
        Assertions.assertThat(response).isNotNull();
        JsonNode jsonNode = objectMapper.readTree(response.getSerializedData());
        String id = jsonNode.get("post").get("id").asText();
        String text = jsonNode.get("post").get("title").asText();
        Assertions.assertThat(id).isEqualTo("0");
        Assertions.assertThat(text).isEqualTo("title");
    }
}