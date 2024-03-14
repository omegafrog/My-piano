package com.omegafrog.My.piano.app.web.domain.sheetpost;

import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.post.Post;
import com.omegafrog.My.piano.app.web.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class SheetPostTest {
    @Test
    @DisplayName("게시글에 댓글을 작성할 수 있어야 한다.")
    void addCommentTest() {
        //given
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(new User())
                .build();
        //when
        Comment comment = Comment.builder()
                .content("comment1")
                .author(new User())
                .build();
        post.addComment(comment);

        //then
        Assertions.assertThat(post.getComments()).isNotEmpty().contains(post.getComments().get(0));
    }

    @Test
    @DisplayName("게시글의 댓글을 삭제할 수 있어야 한다.")
    void deleteCommentTest() {
        //given
        Post post = Post.builder()
                .title("test1")
                .content("content1")
                .author(new User())
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
