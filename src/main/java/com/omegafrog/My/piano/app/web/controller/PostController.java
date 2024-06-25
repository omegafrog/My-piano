package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.post.*;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.service.PostApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostApplicationService postApplicationService;
    @PostMapping("")
    public JsonAPISuccessResponse<Void> writePost(
            @Valid @NotNull  @RequestBody PostRegisterDto post) {
        postApplicationService.writePost(post);
        return new ApiSuccessResponse<>("Write post success");
    }

    @GetMapping("/{id}")
    public JsonAPISuccessResponse<PostDto> findPost(@Valid @NotNull @PathVariable Long id) {
        PostDto postById = postApplicationService.findPostById(id);
        return new ApiSuccessResponse<>("Find post success", postById);
    }

    @GetMapping("")
    public JsonAPISuccessResponse<ReturnPostListDto> findAllPost(
            @Valid @NotNull @PageableDefault(page = 0, size=30) Pageable pageable){
        ReturnPostListDto data = postApplicationService.findPosts(pageable);
        return new ApiSuccessResponse<>("Find all post success", data);
    }

    @PostMapping("/{id}")
    public JsonAPISuccessResponse updatePost(
            @Valid @NotNull @PathVariable Long id,
            @Valid @NotNull @RequestBody UpdatePostDto post) {
        PostDto postDto = postApplicationService.updatePost(id, post);
        return new ApiSuccessResponse("update post success", postDto);
    }

    @DeleteMapping("/{id}")
    public JsonAPISuccessResponse<Void> deletePost(
            @Valid @NotNull @PathVariable Long id) {
        postApplicationService.deletePost(id);
        return new ApiSuccessResponse<>("delete post success");
    }

    @PutMapping("/{id}/like")
    public JsonAPISuccessResponse likePost(
            @Valid @NotNull @PathVariable Long id) {
        postApplicationService.likePost(id);
        return new ApiSuccessResponse("like post success");
    }
    @DeleteMapping("/{id}/like")
    public JsonAPISuccessResponse dislikePost(
            @Valid @NotNull @PathVariable Long id) {
        postApplicationService.dislikePost(id);
        return new ApiSuccessResponse("dislike post success");
    }
    @GetMapping("/{id}/like")
    public JsonAPISuccessResponse isLikedPost(
            @Valid @NotNull @PathVariable Long id){
        boolean isLikedPost = postApplicationService.isLikedPost(id);
        return new ApiSuccessResponse("Check liked post success", isLikedPost);
    }
}
