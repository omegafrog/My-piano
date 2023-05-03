package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.post.dto.UpdatePostDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PostTest {

    @Test
    void update() {
        Post post = Post.builder()
                .title("title")
                .content("content")
                .author(new Author("author", "none"))
                .build();
        String title = "updated";
        String content = "updatedContent";
        UpdatePostDto updated = UpdatePostDto.builder()
                .title(title)
                .content(content)
                .likeCount(1)
                .viewCount(1)
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
                .author(new Author( "author", "none"))
                .build();
        String content = "hi";
        Comment comment = new Comment(
                0L,
                new Author("author1", "none"),
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
                .author(new Author( "author", "none"))
                .build();
        String content = "hi";
        Comment comment = new Comment(
                0L,
                new Author("author1", "none"),
                content);
        int cnt = post.addComment(comment);
        post.deleteComment(0L);
        Assertions.assertThat(post.getComments().size()).isEqualTo(0);
    }
}