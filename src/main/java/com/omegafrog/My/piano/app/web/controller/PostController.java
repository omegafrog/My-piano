package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.post.*;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.service.PostApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostApplicationService postApplicationService;

    @PostMapping("")
    public JsonAPISuccessResponse<Void> writePost(@RequestBody PostRegisterDto post) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.writePost(post, loggedInUser);
        return new ApiSuccessResponse<>("Write post success");
    }

    @GetMapping("/{id}")
    public JsonAPISuccessResponse<PostDto> findPost(@PathVariable Long id)
            throws JsonProcessingException {
        PostDto postById = postApplicationService.findPostById(id);
        return new ApiSuccessResponse<>("Find post success", postById);
    }

    @GetMapping("")
    public JsonAPISuccessResponse<ReturnPostListDto> findAllPost(Pageable pageable) throws JsonProcessingException {
        ReturnPostListDto data = postApplicationService.findPosts(pageable);
        return new ApiSuccessResponse<>("Find all post success", data);
    }

    @PostMapping("/{id}")
    public JsonAPISuccessResponse updatePost(@PathVariable Long id, @RequestBody UpdatePostDto post)
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        PostDto postDto = postApplicationService.updatePost(id, post, loggedInUser);
        return new ApiSuccessResponse("update post success", postDto);
    }

    @DeleteMapping("/{id}")
    public JsonAPISuccessResponse<Void> deletePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.deletePost(id, loggedInUser);
        return new ApiSuccessResponse<>("delete post success");
    }

    @PutMapping("/{id}/like")
    public JsonAPISuccessResponse likePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.likePost(id, loggedInUser);
        return new ApiSuccessResponse("like post success");
    }
    @DeleteMapping("/{id}/like")
    public JsonAPISuccessResponse dislikePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.dislikePost(id, loggedInUser);
        return new ApiSuccessResponse("dislike post success");
    }
    @GetMapping("/{id}/like")
    public JsonAPISuccessResponse isLikedPost(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isLikedPost = postApplicationService.isLikedPost(id, loggedInUser);
        return new ApiSuccessResponse("Check liked post success", isLikedPost);
    }
}
