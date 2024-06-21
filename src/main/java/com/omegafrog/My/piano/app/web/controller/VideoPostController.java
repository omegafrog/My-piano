package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.post.UpdateVideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostDto;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostRegisterDto;
import com.omegafrog.My.piano.app.web.service.VideoPostApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/video-post")
public class VideoPostController {
    private final ObjectMapper objectMapper;
    private final VideoPostApplicationService videoPostApplicationService;

    @PostMapping("")
    public JsonAPISuccessResponse writePost(@RequestBody VideoPostRegisterDto videoPost)
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        VideoPostDto videoPostDto = videoPostApplicationService.writePost(videoPost, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("videoPost", videoPostDto);
        return new ApiSuccessResponse("Write videoPost success", data);
    }

    @GetMapping("/{id}")
    public JsonAPISuccessResponse findPost(@PathVariable Long id)
            throws JsonProcessingException {
        VideoPostDto postById = videoPostApplicationService.findPostById(id);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("videoPost", postById);
        return new ApiSuccessResponse("Find videoPost success", data);
    }
    @GetMapping
    public JsonAPISuccessResponse findAllPosts(Pageable pageable) throws JsonProcessingException {
        List<VideoPostDto> allVideoPosts = videoPostApplicationService.findAllVideoPosts(pageable);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("videoPosts", allVideoPosts);
        return new ApiSuccessResponse("Find all videoPosts success", data);
    }

    @PostMapping("/{id}")
    public JsonAPISuccessResponse updatePost(@PathVariable Long id, @RequestBody UpdateVideoPostDto post)
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        VideoPostDto postDto = videoPostApplicationService.updatePost(id, post, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("videoPost", postDto);
        return new ApiSuccessResponse("Update video post success", data);
    }

    @DeleteMapping("/{id}")
    public JsonAPISuccessResponse deletePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        videoPostApplicationService.deletePost(id, loggedInUser);
        return new ApiSuccessResponse("delete video post success");
    }

    @GetMapping("/{id}/like")
    public JsonAPISuccessResponse likePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        videoPostApplicationService.likePost(id, loggedInUser);
        return new ApiSuccessResponse("Like video post success.");
    }

    @DeleteMapping("/{id}/like")
    public JsonAPISuccessResponse dislikePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        videoPostApplicationService.dislikePost(id, loggedInUser);
        return new ApiSuccessResponse("Dislike video post success");
    }

}
