package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentTargetType;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.service.*;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value={"/api/v1/lesson/{id}/comments",
        "/api/v1/posts/{id}/comments",
        "/api/v1/community/video-post/{id}/comments",
        "/api/v1/sheet-post/{id}/comments"})
public class CommentController {
    private final CommentApplicationService commentApplicationService;

    @PostMapping
    public JsonAPISuccessResponse<List<CommentDto>> addComment(@PathVariable Long id,
                                                               @Validated @RequestBody RegisterCommentDto dto,
                                                               HttpServletRequest request) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();

        List<CommentDto> comments= commentApplicationService.addComment(
                CommentTargetType.of(request),
                id,
                dto,
                loggedInUser);

        return new ApiSuccessResponse<>("Add Comment success.", comments);
    }

    @PostMapping("/{comment-id}")
    public JsonAPISuccessResponse<CommentDto> replyComment(@PathVariable(name = "comment-id")  Long commentId, String content)
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        CommentDto dto = commentApplicationService.replyComment(commentId, content, loggedInUser);

        return new ApiSuccessResponse<>("Reply Comment success.", dto);
    }

    @DeleteMapping("{comment-id}")
    public JsonAPISuccessResponse<Void> deleteComment(
            @PathVariable Long id,
            @PathVariable(name = "comment-id") Long commentId
            ,HttpServletRequest request
    ) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        commentApplicationService.deleteComment(CommentTargetType.of(request),id, commentId, loggedInUser);
        return new ApiSuccessResponse<>("Delete Comment success.");
    }

    @GetMapping
    public JsonAPISuccessResponse<Page<CommentDto>> getComments(
            @PathVariable Long id,
            @PageableDefault(size=10) Pageable pageable)
            throws PersistenceException, AccessDeniedException, JsonProcessingException{
        Page<CommentDto> page = commentApplicationService.getComments(id, pageable);
        return new ApiSuccessResponse<>("Get all comments success.", page);
    }
}
