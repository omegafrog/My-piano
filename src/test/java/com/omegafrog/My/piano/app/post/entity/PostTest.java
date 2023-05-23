package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.dto.UpdatePostDto;
import com.omegafrog.My.piano.app.user.entity.User;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class PostTest {

    @Test
    void update() {
        Post post = Post.builder()
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
        UpdatePostDto updated = UpdatePostDto.builder()
                .title(title)
                .content(content)
                .build();
        Post updatedPost = post.update(updated);

        Assertions.assertThat(updatedPost.getContent()).isEqualTo(content);
        Assertions.assertThat(updatedPost.getTitle()).isEqualTo(title);
    }

    @Test
    void addComment() {
        Post post = Post.builder()
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
        Post post = Post.builder()
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