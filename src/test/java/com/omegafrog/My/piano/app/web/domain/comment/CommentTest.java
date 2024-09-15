package com.omegafrog.My.piano.app.web.domain.comment;

import com.omegafrog.My.piano.app.web.domain.article.Article;
import com.omegafrog.My.piano.app.web.domain.user.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;


class CommentTest {

    @Test
    @DisplayName("Comment의 likeCount가 증가해야 한다")
    void increaseLikeCount() {
        //given
        Comment comment = new Comment(1L, new User(), "test comment", new Article(), null);

        //when
        comment.increaseLikeCount();
        //then
        Assertions.assertThat(comment).extracting("likeCount").isEqualTo(1);
    }

    @Test
    @DisplayName("Comment의 likeCount가 감소해야 한다")
    void decreaseLikeCount() {
        //given
        Comment comment = new Comment(1L, new User(), "test comment", new Article(), null);
        ReflectionTestUtils.setField(comment, "likeCount", 2);
        //when
        comment.decreaseLikeCount();
        //then
        Assertions.assertThat(comment).extracting("likeCount").isEqualTo(1);
    }
}