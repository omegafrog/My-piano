package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.dto.post.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostRegisterDto;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.VideoPostApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/video-post")
public class VideoPostController {
    private final VideoPostApplicationService videoPostApplicationService;

    @PostMapping("")
    public JsonAPIResponse writePost(@RequestBody VideoPostRegisterDto videoPost) {
        VideoPostDto videoPostDto = videoPostApplicationService.writePost(videoPost);
        return new ApiResponse("Write videoPost success", videoPostDto);
    }

    @GetMapping("/{id}")
    public JsonAPIResponse findPost(@PathVariable(name="id") Long id) {
        VideoPostDto postById = videoPostApplicationService.findPostById(id);
        return new ApiResponse("Find videoPost success", postById);
    }
    @GetMapping
    public JsonAPIResponse findAllPosts(
            @PageableDefault(page = 0, size = 30) Pageable pageable) {
        List<VideoPostDto> allVideoPosts = videoPostApplicationService.findAllVideoPosts(pageable);
        return new ApiResponse("Find all videoPosts success", allVideoPosts);
    }

    @PostMapping("/{id}")
    public JsonAPIResponse updatePost(@PathVariable(name="id") Long id, @RequestBody UpdateVideoPostDto post) {
        VideoPostDto postDto = videoPostApplicationService.updatePost(id, post);
        return new ApiResponse("Update video post success", postDto);
    }

    @DeleteMapping("/{id}")
    public JsonAPIResponse deletePost(@PathVariable(name="id") Long id) {
        videoPostApplicationService.deletePost(id);
        return new ApiResponse("delete video post success");
    }

    @GetMapping("/{id}/like")
    public JsonAPIResponse likePost(@PathVariable(name="id") Long id) {
        videoPostApplicationService.likePost(id);
        return new ApiResponse("Like video post success.");
    }

    @DeleteMapping("/{id}/like")
    public JsonAPIResponse dislikePost(@PathVariable(name="id") Long id) {
        videoPostApplicationService.dislikePost(id);
        return new ApiResponse("Dislike video post success");
    }
}
