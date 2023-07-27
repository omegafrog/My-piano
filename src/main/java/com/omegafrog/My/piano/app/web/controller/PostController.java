package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.post.CommentDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.response.APISuccessResponse;
import com.omegafrog.My.piano.app.web.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.PostApplicationService;
import jakarta.persistence.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class PostController {

    private final ObjectMapper objectMapper;
    private final PostApplicationService postApplicationService;


    @PostMapping("")
    public JsonAPIResponse writePost(@RequestBody PostRegisterDto post)
            throws PersistenceException, JsonProcessingException, AccessDeniedException {
        User loggedInUser = getLoggedInUser();
        PostDto postDto = postApplicationService.writePost(post, loggedInUser);
        Map<String, Object> data = getStringObjectMap("post", postDto);
        return new APISuccessResponse("Write post success", data, objectMapper );
    }

    @GetMapping("/{id}")
    public JsonAPIResponse findPost(@PathVariable Long id)
            throws PersistenceException, JsonProcessingException {
        PostDto postById = postApplicationService.findPostById(id);
        Map<String, Object> data = getStringObjectMap("post", postById);
        return new APISuccessResponse("Find post success", data, objectMapper);
    }

    @PostMapping("/{id}")
    public JsonAPIResponse updatePost(@PathVariable Long id, @RequestBody UpdatePostDto post)
            throws JsonProcessingException, PersistenceException, AccessDeniedException {
        User loggedInUser = getLoggedInUser();
        PostDto postDto = postApplicationService.updatePost(id, post, loggedInUser);
        Map<String, Object> data = getStringObjectMap("post", postDto);
        return new APISuccessResponse("update post success", data, objectMapper);
    }

    @DeleteMapping("/{id}")
    public JsonAPIResponse deletePost(@PathVariable Long id)
            throws PersistenceException, AccessDeniedException {
        User loggedInUser = getLoggedInUser();
        postApplicationService.deletePost(id, loggedInUser);
        return new APISuccessResponse("delete post success");
    }

    @PostMapping("/{id}/comment")
    public JsonAPIResponse addComment(@RequestBody CommentDto dto, @PathVariable Long id)
            throws JsonProcessingException, AccessDeniedException, PersistenceException {
        User loggedInUser = getLoggedInUser();
        List<CommentDto> commentDtos = postApplicationService.addComment(id, loggedInUser, dto);
        Map<String, Object> data = getStringObjectMap("comments", commentDtos);
        return new APISuccessResponse("add comment success.", data, objectMapper);
    }

    @DeleteMapping("/{id}/comment/{comment-id}")
    public JsonAPIResponse deleteComment(@PathVariable Long id, @PathVariable(name = "comment-id") Long commentId)
            throws JsonProcessingException, AccessDeniedException, PersistenceException {
        User loggedInUser = getLoggedInUser();
        List<CommentDto> commentDtos = postApplicationService.deleteComment(id, commentId, loggedInUser);
        Map<String, Object> data = getStringObjectMap("comments", commentDtos);
        return new APISuccessResponse("delete comment success.", data, objectMapper);
    }

    @GetMapping("/{id}/like")
    public JsonAPIResponse likePost(@PathVariable Long id)
            throws PersistenceException, AccessDeniedException {
        User loggedInUser = getLoggedInUser();
        postApplicationService.likePost(id, loggedInUser);
        return new APISuccessResponse("Like post success.");
    }

    @DeleteMapping("/{id}/like")
    public JsonAPIResponse dislikePost(@PathVariable Long id)
            throws PersistenceException, AccessDeniedException {
        User loggedInUser = getLoggedInUser();
        postApplicationService.dislikePost(id, loggedInUser);
        return new APISuccessResponse("Dlslike post success");
    }

    public User getLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null)
            throw new AccessDeniedException("authentication is null");
        return ((SecurityUser) authentication.getPrincipal()).getUser();
    }

    public Map<String, Object> getStringObjectMap(String keyName, Object object) {
        Map<String, Object> data = new HashMap<>();
        data.put(keyName, object);
        return data;
    }

}
