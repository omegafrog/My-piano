package com.omegafrog.My.piano.app.web.domain.videoPost;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.post.VideoPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class VideoPostTest {
    @Test
    @DisplayName("게시글에 댓글을 작성할 수 있어야 한다.")
    void addCommentTest() {
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(new User())
                .videoUrl("url")
                .build();
        //when
        User commentAuthor = new User();
        ReflectionTestUtils.setField(commentAuthor, "id", 1L);
        Comment comment = Comment.builder()
                .content("comment1")
                .author(commentAuthor)
                .build();
        ReflectionTestUtils.setField(comment, "id", 1L);
        post.addComment(comment);
        //then
        Assertions.assertThat(post.getComments()).isNotEmpty();
        Assertions.assertThat(post.getComments().get(0)).isEqualTo(comment);
    }

    @Test
    @DisplayName("게시글의 댓글을 삭제할 수 있어야 한다.")
    void deleteCommentTest() {
        //given
        VideoPost post = VideoPost.builder()
                .title("title")
                .content("content")
                .author(new User())
                .videoUrl("url")
                .build();
        User commentAuthor = new User();
        ReflectionTestUtils.setField(commentAuthor, "id", 1L);
        Comment comment = Comment.builder()
                .content("comment1")
                .author(commentAuthor)
                .build();
        ReflectionTestUtils.setField(comment, "id", 1L);
        post.addComment(comment);

        //when
        post.deleteComment(comment.getId(), commentAuthor);

        //then
        Assertions.assertThat(post.getComments()).isEmpty();
    }
}
