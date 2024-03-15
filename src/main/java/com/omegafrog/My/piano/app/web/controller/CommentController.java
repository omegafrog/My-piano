package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.CommentDto;
import com.omegafrog.My.piano.app.web.dto.comment.RegisterCommentDto;
import com.omegafrog.My.piano.app.web.service.*;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final ObjectMapper objectMapper;
    @Autowired
    private PostApplicationService postApplicationService;
    @Autowired
    private VideoPostApplicationService videoPostApplicationService;
    @Autowired
    private LessonService lessonService;
    @Autowired
    private SheetPostApplicationService sheetPostApplicationService;


    @PostMapping(value = {"/lesson/{id}/comment",
            "/community/posts/{id}/comment",
            "/community/video-post/{id}/comment",
            "/sheet/{id}/comment"})
    public JsonAPIResponse addComment(
            @PathVariable Long id,
            @Validated @RequestBody RegisterCommentDto dto,
            HttpServletRequest request
    ) throws JsonProcessingException, MalformedURLException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();

        CommentHandler commentHandler = getCommentHandler(request.getRequestURI());
        List<CommentDto> commentDtos = commentHandler.addComment(id, dto, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("comments", commentDtos);
        return new APISuccessResponse("Add Comment success.", data);
    }

    @DeleteMapping(value = {"/lesson/{id}/comment/{comment-id}",
            "/sheet/{id}/comment/{comment-id}",
            "/community/posts/{id}/comment/{comment-id}",
            "/community/video-post/{id}/comment/{comment-id}"})
    public JsonAPIResponse deleteComment(
            @PathVariable Long id,
            @PathVariable(name = "comment-id") Long commentId,
            HttpServletRequest request
    ) throws JsonProcessingException, MalformedURLException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        CommentHandler commentHandler = getCommentHandler(request.getRequestURI());
        List<CommentDto> commentDtos = commentHandler.deleteComment(id, commentId, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("comments", commentDtos);
        return new APISuccessResponse("Delete Comment success.", data);
    }

    @GetMapping(value = {"/lesson/{id}/comment/{comment-id}/like",
            "/sheet/{id}/comment/{comment-id}/like",
            "/community/posts/{id}/comment/{comment-id}/like",
            "/community/video-post/{id}/comment/{comment-id}/like"})
    public JsonAPIResponse likeComments(
            @PathVariable Long id,
            @PathVariable(name = "comment-id") Long commentId,
            HttpServletRequest request)
            throws  PersistenceException, MalformedURLException {
        CommentHandler commentHandler = getCommentHandler(request.getRequestURI());
        commentHandler.likeComment(id, commentId);
        return new APISuccessResponse("Like comment success.");
    }

    @GetMapping(value = {"/lesson/{id}/comment/{comment-id}/dislike",
            "/sheet/{id}/comment/{comment-id}/dislike",
            "/community/posts/{id}/comment/{comment-id}/dislike",
            "/community/video-post/{id}/comment/{comment-id}/dislike"})
    public JsonAPIResponse dislikeComments(
            @PathVariable Long id,
            @PathVariable(name = "comment-id") Long commentId,
            HttpServletRequest request)
            throws  PersistenceException, MalformedURLException {
        CommentHandler commentHandler = getCommentHandler(request.getRequestURI());
        commentHandler.dislikeComment(id, commentId);
        return new APISuccessResponse("dislike comment success.");
    }

    @GetMapping(value = {"/lesson/{id}/comments",
            "/community/posts/{id}/comments",
            "/community/video-post/{id}/comments",
            "/sheet/{id}/comments"})
    public JsonAPIResponse getComments(
            @PathVariable Long id,
            @PageableDefault(size=10) Pageable pageable,
            HttpServletRequest request)
            throws PersistenceException, AccessDeniedException, JsonProcessingException, MalformedURLException {
        CommentHandler commentHandler = getCommentHandler(request.getRequestURI());
        List<CommentDto> comments = commentHandler.getComments(id, pageable);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("comments", comments);
        return new APISuccessResponse("Get all comments success.", data);
    }

    private CommentHandler getCommentHandler(String url) throws MalformedURLException {
        String mainResource = url.subSequence(1, url.indexOf("/",1)).toString();
        if (mainResource.equals("community")) {
            url = url.substring(url.indexOf("/", 1));
            mainResource = url.substring(1, url.indexOf("/", 1));
        }
        switch (mainResource) {
            case "lesson":
                return lessonService;
            case "posts":
                return postApplicationService;
            case "video-post":
                return videoPostApplicationService;
            case "sheet":
                return sheetPostApplicationService;
            default:
                throw new MalformedURLException("잘못된 mainResource입니다." + mainResource);
        }
    }

}
