package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.post.PostRegisterDto;
import com.omegafrog.My.piano.app.web.dto.post.UpdatePostDto;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.PostApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/community/post")
@RequiredArgsConstructor
public class PostController {

    private final ObjectMapper objectMapper;
    private final PostApplicationService postApplicationService;


    @PostMapping("")
    public JsonAPIResponse writePost(@RequestBody PostRegisterDto post)
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        PostDto postDto = postApplicationService.writePost(post, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("post", postDto);
        return new APISuccessResponse("Write post success", data);
    }

    //TODO : 모든 post pagination해서 조회해야함

    @GetMapping("/{id}")
    public JsonAPIResponse findPost(@PathVariable Long id)
            throws JsonProcessingException {
        PostDto postById = postApplicationService.findPostById(id);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("post", postById);
        return new APISuccessResponse("Find post success", data);
    }

    @PostMapping("/{id}")
    public JsonAPIResponse updatePost(@PathVariable Long id, @RequestBody UpdatePostDto post)
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        PostDto postDto = postApplicationService.updatePost(id, post, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("post", postDto);
        return new APISuccessResponse("update post success", data);
    }

    @DeleteMapping("/{id}")
    public JsonAPIResponse deletePost(@PathVariable Long id) {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.deletePost(id, loggedInUser);
        return new APISuccessResponse("delete post success");
    }

    @GetMapping("/{id}/like")
    public JsonAPIResponse likePost(@PathVariable Long id){
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        postApplicationService.likePost(id, loggedInUser);
        return new APISuccessResponse("like post success");
    }
}
