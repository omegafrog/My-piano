package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.CommentTargetType;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value={"/api/v1/lesson/{id}/comments",
        "/api/v1/community/posts/{id}/comments",
        "/api/v1/video-post/{id}/comments",
        "/api/v1/sheet-post/{id}/comments"})
public class CommentController {
    private final CommentApplicationService commentApplicationService;

    @PostMapping
    public JsonAPIResponse<List<CommentDto>> addComment(
            @Valid @NotNull @PathVariable(name = "id") Long id,
            @Valid @NotNull @RequestBody RegisterCommentDto dto,
            HttpServletRequest request
    ){
        List<CommentDto> comments= commentApplicationService.addComment(CommentTargetType.of(request), id, dto);
        return new ApiResponse<>("Add Comment success.", comments);
    }

    @PostMapping("/{comment-id}")
    public JsonAPIResponse<CommentDto> replyComment(
            @Valid @NotNull @PathVariable(name = "comment-id")  Long commentId,
            @Valid @NotNull String content
    ) {
        CommentDto dto = commentApplicationService.replyComment(commentId, content);
        return new ApiResponse<>("Reply Comment success.", dto);
    }

    @DeleteMapping("{comment-id}")
    public JsonAPIResponse<Void> deleteComment(
            @Valid @NotNull @PathVariable(name="id") Long id,
            @Valid @NotNull @PathVariable(name = "comment-id") Long commentId,
            HttpServletRequest request
    ) {
        commentApplicationService.deleteComment(CommentTargetType.of(request),id, commentId);
        return new ApiResponse<>("Delete Comment success.");
    }

    @GetMapping
    public JsonAPIResponse<Page<CommentDto>> getComments(
            @Valid @NotNull @PathVariable(name="id") Long id,
            @Valid @NotNull @PageableDefault(size=10) Pageable pageable) {
        Page<CommentDto> page = commentApplicationService.getComments(id, pageable);
        return new ApiResponse<>("Get all comments success.", page);
    }
}
