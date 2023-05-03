package com.omegafrog.My.piano.app.post.entity;

import com.omegafrog.My.piano.app.post.dto.UpdatePostDto;
import com.omegafrog.My.piano.app.post.dto.UpdateVideoPostDto;
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
                .author(new Author("author", "none"))
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
                .author(new Author( "author", "none"))
                .videoUrl("url1")
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
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(new Author( "author", "none"))
                .videoUrl("url1")
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