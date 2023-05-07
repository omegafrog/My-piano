package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.post.dto.UpdatePostDto;
import com.omegafrog.My.piano.app.post.dto.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.user.entity.User;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import org.assertj.core.api.Assertions;
import org.hibernate.sql.Update;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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