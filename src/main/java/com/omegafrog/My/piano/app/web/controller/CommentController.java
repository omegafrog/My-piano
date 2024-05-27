package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentTargetType;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.service.*;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.RequestContextFilter;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping(value={"/api/v1/lesson/{id}/comments",
        "/api/v1/posts/{id}/comments",
        "/api/v1/community/video-post/{id}/comments",
        "/api/v1/sheet-post/{id}/comments"})
public class CommentController {
    private final CommentApplicationService commentApplicationService;

    @PostMapping
    public JsonAPIResponse<List<CommentDto>> addComment(@PathVariable Long id,
                                                        @Validated @RequestBody RegisterCommentDto dto,
                                                        HttpServletRequest request) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();

        List<CommentDto> comments= commentApplicationService.addComment(
                CommentTargetType.of(request),
                id,
                dto,
                loggedInUser);

        return new APISuccessResponse<>("Add Comment success.", comments);
    }

    @PostMapping("/{comment-id}")
    public JsonAPIResponse<CommentDto> replyComment(@PathVariable(name = "comment-id")  Long commentId, String content)
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        CommentDto dto = commentApplicationService.replyComment(commentId, content, loggedInUser);

        return new APISuccessResponse<>("Reply Comment success.", dto);
    }

    @DeleteMapping("{comment-id}")
    public JsonAPIResponse<Void> deleteComment(
            @PathVariable Long id,
            @PathVariable(name = "comment-id") Long commentId
            ,HttpServletRequest request
    ) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        commentApplicationService.deleteComment(CommentTargetType.of(request),id, commentId, loggedInUser);
        return new APISuccessResponse<>("Delete Comment success.");
    }

    @GetMapping
    public JsonAPIResponse<Page<CommentDto>> getComments(
            @PathVariable Long id,
            @PageableDefault(size=10) Pageable pageable)
            throws PersistenceException, AccessDeniedException, JsonProcessingException{
        Page<CommentDto> page = commentApplicationService.getComments(id, pageable);
        return new APISuccessResponse<>("Get all comments success.", page);
    }
}
