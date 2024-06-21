package com.omegafrog.My.piano.app.web.domain.article;

import com.omegafrog.My.piano.app.utils.exception.article.CannotDecreaseLikeCountException;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.persistence.EntityNotFoundException;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

class ArticleTest {

    @Test
    void increaseLikedCount() {
        // given
        Article article = new Article();
        int before = article.getLikeCount();

        // when
        article.increaseLikedCount();

        // then
        assertEquals(before + 1, article.getLikeCount());
    }

    @Test
    void decreaseLikedCount() {
        // given
        Article article = new Article();
        article.increaseLikedCount();
        int before = article.getLikeCount();

        // when
        article.decreaseLikedCount();

        // then
        assertEquals(before - 1, article.getLikeCount());
    }
    @Test
    void cannotDecreaseZeroLikedCount(){
        // given
        Article article = new Article();

        // when
        assertThatThrownBy(()-> article.decreaseLikedCount())
                .isInstanceOf(CannotDecreaseLikeCountException.class);

        // then
        assertEquals(0, article.getLikeCount());
    }

    @Test
    void increaseViewCount() {
        // given
        Article article = new Article();
        int before = article.getViewCount();

        // when
        article.increaseViewCount();

        // then
        assertEquals(before + 1, article.getViewCount());
    }

    @Test
    void addComment() {
        Article article = new Article();
        Comment comment = Comment.builder()
                        .author(new User())
                        .content("content")
                        .build();

        article.addComment(comment);
        assertEquals(1, article.getComments().size());
        assertEquals(comment, article.getComments().get(0));
    }

    @Test
    void deleteComment() {
        // given
        Article article = new Article();
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);

        Comment comment = Comment.builder()
                .id(1L)
                .author(user)
                .content("content")
                .build();
        article.addComment(comment);

        // when
        article.deleteComment(comment.getId(), user);

        // then
        assertEquals(0, article.getComments().size());
    }

    @Test
    void deleteNonExistCommentTest(){
        Article article = new Article();
        assertThrows(EntityNotFoundException.class, ()->article.deleteComment(1L, new User()));
    }

    @Test
    void deleteOtherUsersCommentTest(){
        Article article = new Article();
        User user = new User();
        User others = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(others, "id", 2L);

        Comment comment = Comment.builder()
                .id(1L)
                .author(others)
                .content("content")
                .build();
        article.addComment(comment);
        assertThrows(AccessDeniedException.class, ()->article.deleteComment(comment.getId(), user));
    }
}