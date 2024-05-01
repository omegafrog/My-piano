package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.utils.exception.message.ExceptionMessage;
import com.omegafrog.My.piano.app.web.domain.comment.Comment;
import com.omegafrog.My.piano.app.web.domain.comment.CommentRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentApplicationService {

    private final CommentRepository commentRepository;

    /**
     * 글에 달린 모든 댓글을 조회한다
     * @param articleId 글 id
     * @param pageable {@link Pageable} 객체
     * @return {@link Page}<{@link CommentDto}> 조회한 댓글이 담긴 Page 객체
     */
    public Page<CommentDto> getComments(Long articleId, Pageable pageable) {
        Page<Comment> allByTargetId = commentRepository.findAllByTargetId(articleId, pageable);

        return PageableExecutionUtils.getPage(
                allByTargetId.get().map(Comment::toDto).toList(),
                pageable,
                allByTargetId::getTotalElements);
    }


    @Override
    public List<CommentDto> addComment(Long id, RegisterCommentDto dto, User loggedInUser) {
        SheetPost sheetPost = sheetPostRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find sheet post entity : " + id));
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionMessage.ENTITY_NOT_FOUND_USER));

        Comment savedComment = commentRepository.save(
                Comment.builder()
                        .content(dto.getContent())
                        .author(user)
                        .build());
        sheetPost.addComment(savedComment);
        return sheetPost.getComments().stream().map(Comment::toDto).toList();
    }
}
