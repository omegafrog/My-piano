package com.omegafrog.My.piano.app.web.infrastructure.post;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.post.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.infra.user.JpaUserRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.user.SimpleJpaUserRepository;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.article.Comment;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.post.VideoPostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VideoPostRepositoryTest {

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private SimpleJpaUserRepository jpaUserRepository;

    private UserRepository userRepository;

    private User user1;

    @BeforeAll
    void settings() {
        userRepository = new JpaUserRepositoryImpl(jpaUserRepository);
        User build = User.builder()
                .name("user1")
                .profileSrc("profile1")
                .loginMethod(LoginMethod.EMAIL)
                .phoneNum(PhoneNum.builder()
                        .phoneNum("010-1111-1112")
                        .isAuthorized(false)
                        .build())
                .email("user1@gmail.com")
                .cart(new Cart())
                .build();
        user1 = userRepository.save(build);
    }

    @AfterEach
    void clearRepository() {
        videoPostRepository.deleteAll();
    }

    @AfterAll
    void clearAllRepository() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("비디오 커뮤니티 게시글을 추가하고 조회할 수 있어야 한다.")
    void saveNFindTest() {
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(user1)
                .videoUrl("url")
                .build();

        //when
        VideoPost saved = videoPostRepository.save(post);
        //then
        Assertions.assertThat(saved).isEqualTo(post);
    }

    @Test
    @DisplayName("비디오 커뮤니티 게시글을 수정할 수 있어야 한다.")
    void updateTest() {
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(user1)
                .videoUrl("url")
                .build();
        VideoPost saved = videoPostRepository.save(post);
        //when
        String changedTitle = "changedTitle";
        String changedContent = "changedContent";
        String changedUrl = "changedUrl";
        UpdateVideoPostDto dto = UpdateVideoPostDto.builder()
                .title(changedTitle)
                .content(changedContent)
                .videoUrl(changedUrl)
                .build();
        VideoPost updated = saved.update(dto);
        VideoPost updatedVideoPost = videoPostRepository.save(updated);
        //then
        Assertions.assertThat(updatedVideoPost).isEqualTo(saved);
        Assertions.assertThat(updatedVideoPost.getTitle())
                .isEqualTo(changedTitle);
        Assertions.assertThat(updatedVideoPost.getContent())
                .isEqualTo(changedContent);
        Assertions.assertThat(updatedVideoPost.getVideoUrl())
                .isEqualTo(changedUrl);
    }

    @Test
    @DisplayName("게시글에 댓글을 작성할 수 있어야 한다.")
    void addCommentTest() {
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(user1)
                .videoUrl("url")
                .build();
        VideoPost saved = videoPostRepository.save(post);
        //when
        Comment comment = Comment.builder()
                .content("comment1")
                .author(user1)
                .build();
        saved.addComment(comment);
        VideoPost commentAdded = videoPostRepository.save(saved);
        //then
        Assertions.assertThat(commentAdded.getComments().size()).isGreaterThanOrEqualTo(1);
        Assertions.assertThat(commentAdded.getComments().get(0).getContent()).isEqualTo("comment1");
    }

    @Test
    @DisplayName("게시글의 댓글을 삭제할 수 있어야 한다.")
    void deleteCommentTest() {
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(user1)
                .videoUrl("url")
                .build();
        VideoPost saved = videoPostRepository.save(post);
        Comment comment = Comment.builder()
                .content("comment1")
                .author(user1)
                .build();
        saved.addComment(comment);
        VideoPost commentAdded = videoPostRepository.save(saved);
        //when
        commentAdded.deleteComment(commentAdded.getComments().get(0).getId(), commentAdded.getComments().get(0).getAuthor());
        VideoPost deletedComment = videoPostRepository.save(commentAdded);

        //then
        Assertions.assertThat(deletedComment.getComments().size()).isEqualTo(0);
    }

}