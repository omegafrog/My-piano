package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.post.*;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.PostApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostApplicationService postApplicationService;

    @PostMapping("")
    public JsonAPIResponse<Void> writePost(@RequestBody PostRegisterDto post) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.writePost(post, loggedInUser);
        return new APISuccessResponse<>("Write post success");
    }

    @GetMapping("/{id}")
    public JsonAPIResponse<ReturnPostDto> findPost(@PathVariable Long id)
            throws JsonProcessingException {
        ReturnPostDto postById = postApplicationService.findPostById(id);
        return new APISuccessResponse<>("Find post success", postById);
    }

    @GetMapping("")
    public JsonAPIResponse<ReturnPostListDto> findAllPost(Pageable pageable) throws JsonProcessingException {
        ReturnPostListDto data = postApplicationService.findPosts(pageable);
        return new APISuccessResponse<>("Find all post success", data);
    }

    @PostMapping("/{id}")
    public JsonAPIResponse updatePost(@PathVariable Long id, @RequestBody UpdatePostDto post)
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        PostDto postDto = postApplicationService.updatePost(id, post, loggedInUser);
        return new APISuccessResponse("update post success", postDto);
    }

    @DeleteMapping("/{id}")
    public JsonAPIResponse<Void> deletePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.deletePost(id, loggedInUser);
        return new APISuccessResponse<>("delete post success");
    }

    @PutMapping("/{id}/like")
    public JsonAPIResponse likePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.likePost(id, loggedInUser);
        return new APISuccessResponse("like post success");
    }
    @DeleteMapping("/{id}/like")
    public JsonAPIResponse dislikePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.dislikePost(id, loggedInUser);
        return new APISuccessResponse("dislike post success");
    }
    @GetMapping("/{id}/like")
    public JsonAPIResponse isLikedPost(@PathVariable Long id) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        boolean isLikedPost = postApplicationService.isLikedPost(id, loggedInUser);
        return new APISuccessResponse("Check liked post success", isLikedPost);
    }
}
