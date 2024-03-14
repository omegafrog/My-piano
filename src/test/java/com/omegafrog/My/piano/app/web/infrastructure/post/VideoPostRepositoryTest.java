package com.omegafrog.My.piano.app.web.infrastructure.post;

import com.omegafrog.My.piano.app.DataJpaUnitConfig;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.post.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.infra.user.JpaUserRepositoryImpl;
import com.omegafrog.My.piano.app.web.infra.user.SimpleJpaUserRepository;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.post.VideoPostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Optional;

@DataJpaTest
@Import(DataJpaUnitConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VideoPostRepositoryTest {

    @Autowired
    private VideoPostRepository videoPostRepository;

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
        saved.update(dto);

        //then
        Optional<VideoPost> byId = videoPostRepository.findById(saved.getId());
        Assertions.assertThat(byId).get().isEqualTo(saved)
                .extracting("title").isEqualTo(changedTitle);
        Assertions.assertThat(byId).get().isEqualTo(saved)
                .extracting("content").isEqualTo(changedContent);
        Assertions.assertThat(byId).get().isEqualTo(saved)
                .extracting("videoUrl").isEqualTo(changedUrl);
    }



}