package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.dto.post.ReturnPostListDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.PostApplicationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/community/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostApplicationService postApplicationService;

    @PostMapping("")
    public JsonAPIResponse<PostDto> writePost(
            @Valid @NotNull @RequestBody PostRegisterDto post) {
        PostDto postDto = postApplicationService.writePost(post);
        return new ApiResponse<>("Write post success", postDto);
    }

    @GetMapping("/{id}")
    public JsonAPIResponse<PostDto> findPost(@Valid @NotNull @PathVariable Long id) {
        PostDto postById = postApplicationService.findPostById(id);
        return new ApiResponse<>("Find post success", postById);
    }

    @GetMapping("")
    public JsonAPIResponse<ReturnPostListDto> findAllPost(
            @Valid @NotNull @PageableDefault(size = 30) Pageable pageable) {
        ReturnPostListDto data = postApplicationService.findPosts(pageable);
        return new ApiResponse<>("Find all post success", data);
    }

    @PostMapping("/{id}")
    public JsonAPIResponse<PostDto> updatePost(
            @Valid @NotNull @PathVariable Long id,
            @Valid @NotNull @RequestBody UpdatePostDto post) {
        PostDto postDto = postApplicationService.updatePost(id, post);
        return new ApiResponse<>("update post success", postDto);
    }

    @DeleteMapping("/{id}")
    public JsonAPIResponse<Void> deletePost(
            @Valid @NotNull @PathVariable Long id) {
        postApplicationService.deletePost(id);
        return new ApiResponse<>("delete post success");
    }

    @PutMapping("/{id}/like")
    public JsonAPIResponse<Void> likePost(
            @Valid @NotNull @PathVariable Long id) {
        postApplicationService.likePost(id);
        return new ApiResponse<>("like post success");
    }

    @DeleteMapping("/{id}/like")
    public JsonAPIResponse<Void> dislikePost(
            @Valid @NotNull @PathVariable Long id) {
        postApplicationService.dislikePost(id);
        return new ApiResponse<>("dislike post success");
    }

    @GetMapping("/{id}/like")
    public JsonAPIResponse<Boolean> isLikedPost(
            @Valid @NotNull @PathVariable Long id) {
        boolean isLikedPost = postApplicationService.isLikedPost(id);
        return new ApiResponse<>("Check liked post success", isLikedPost);
    }
}
