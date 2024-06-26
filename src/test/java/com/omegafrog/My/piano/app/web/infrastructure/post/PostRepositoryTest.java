package com.omegafrog.My.piano.app.web.infrastructure.post;

import com.omegafrog.My.piano.app.DataJpaUnitConfig;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.infra.post.JpaPostRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.post.SimpleJpaPostRepository;
import com.omegafrog.My.piano.app.web.infra.sheetPost.SimpleJpaSheetPostRepository;
import com.omegafrog.My.piano.app.web.infra.user.JpaUserRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.user.SimpleJpaUserRepository;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@DataJpaTest
@Import(DataJpaUnitConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PostRepositoryTest {

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;

    @BeforeEach
    void settings() {
        User build = User.builder()
                .name("user1")
                .profileSrc("profile1")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-1112")
                        .build())
                .email("user1@gmail.com")
                .cart(new Cart())
                .build();
        user1 = userRepository.save(build);
    }

    @Test
    @DisplayName("게시글을 작성하고 조회할 수 있어야 한다")
    void saveNFindPostTest() {
        //given
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(user1)
                .build();
        //when
        Post saved = postRepository.save(post);
        //then
        Optional<Post> founded = postRepository.findById(saved.getId());
        Assertions.assertThat(founded).get().isEqualTo(saved);
    }

    @Test
    @DisplayName("게시글을 수정할 수 있어야 한다.")
    void updatePostTest() {
        //given
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(user1)
                .build();
        Post saved = postRepository.save(post);
        //when
        UpdatePostDto updatePostDto = UpdatePostDto.builder()
                .title("updated")
                .content("updatedContent")
                .build();
        Post updatedPost = post.update(updatePostDto);
        //then
        Optional<Post> byId = postRepository.findById(saved.getId());
        Assertions.assertThat(byId).get().isEqualTo(updatedPost);
        Assertions.assertThat(byId).get().extracting("content").isEqualTo("updatedContent");
    }

    @Test
    @DisplayName("게시글을 삭제할 수 있어야 한다")
    void deletePostTest() {
        //given
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(user1)
                .build();
        Post saved = postRepository.save(post);
        //when
        postRepository.deleteById(saved.getId());
        //then
        Optional<Post> byId = postRepository.findById(saved.getId());
        Assertions.assertThat(byId).isEmpty();
    }



}