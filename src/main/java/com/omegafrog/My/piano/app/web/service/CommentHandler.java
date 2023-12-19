package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentHandler {
    List<CommentDto> addComment(Long articleId, RegisterCommentDto dto, User loggedInUser);
    List<CommentDto> deleteComment(Long articleId, Long commentId, User loggedInUser);

    void likeComment(Long articleId, Long commentId);
    void dislikeComment(Long articleId, Long commentId);
    List<CommentDto> getComments(Long articleId, Pageable pageable);
}
