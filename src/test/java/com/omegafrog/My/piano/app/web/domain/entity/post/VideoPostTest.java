package com.omegafrog.My.piano.app.web.domain.entity.post;

import com.omegafrog.My.piano.app.web.dto.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import com.omegafrog.My.piano.app.web.domain.post.Comment;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class VideoPostTest {

    @Test
    void update() {
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(User.builder()
                        .name("user1")
                        .profileSrc("profile1")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-1112")
                                .isAuthorized(false)
                                .build())
                        .build())
                .build();
        String title = "updated";
        String content = "updatedContent";
        UpdateVideoPostDto updated = UpdateVideoPostDto.builder()
                .title(title)
                .content(content)
                .videoUrl("none")
                .build();
        VideoPost updatedVideoPost = post.update(updated);

        Assertions.assertThat(updatedVideoPost.getContent()).isEqualTo(content);
        Assertions.assertThat(updatedVideoPost.getTitle()).isEqualTo(title);
    }

    @Test
    void addComment() {
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(User.builder()
                        .name("user1")
                        .profileSrc("profile1")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-1112")
                                .isAuthorized(false)
                                .build())
                        .build())
                .videoUrl("url1")
                .build();
        String content = "hi";
        Comment comment = new Comment(
                0L,
                User.builder()
                        .name("user1")
                        .profileSrc("profile1")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-1112")
                                .isAuthorized(false)
                                .build())
                        .build(),
                content
        );
        int cnt = post.addComment(comment);
        Assertions.assertThat(cnt).isEqualTo(1);
        Assertions.assertThat(post.getComments().get(0).getContent()).isEqualTo(content);
    }

    @Test
    void deleteComment() {
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(User.builder()
                        .name("user1")
                        .profileSrc("profile1")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-1112")
                                .isAuthorized(false)
                                .build())
                        .build())
                .videoUrl("url1")
                .build();
        String content = "hi";
        Comment comment = new Comment(
                0L,
                User.builder()
                        .name("user1")
                        .profileSrc("profile1")
                        .loginMethod(LoginMethod.EMAIL)
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("010-1111-1112")
                                .isAuthorized(false)
                                .build())
                        .build(),
                content);
        int cnt = post.addComment(comment);
        post.deleteComment(0L);
        Assertions.assertThat(post.getComments().size()).isEqualTo(0);
    }
}