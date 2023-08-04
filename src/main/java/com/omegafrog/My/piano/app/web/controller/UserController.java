package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.user.UpdateUserDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import com.omegafrog.My.piano.app.web.util.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.util.response.APISuccessResponse;
import com.omegafrog.My.piano.app.web.util.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.UserApplicationService;
import com.omegafrog.My.piano.app.web.util.response.ResponseUtil;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    @Autowired
    private ObjectMapper objectMapper;

    private final UserApplicationService userService;

    @PostMapping("/cash")
    public JsonAPIResponse chargeCash(@RequestBody int cash) throws JsonProcessingException , PersistenceException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        int chargedCash = userService.chargeCash(cash, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("chargedCash", chargedCash);
        return new APISuccessResponse("Charge cash " + cash + " success.", data, objectMapper);
    }
    @GetMapping("/community/posts")
    public JsonAPIResponse getMyCommunityPosts()
            throws AccessDeniedException, JsonProcessingException, PersistenceException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<PostDto> myCommunityPosts = userService.getMyCommunityPosts(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("posts", myCommunityPosts);
        return new APISuccessResponse("Get community posts success.", data, objectMapper);
    }

    @GetMapping("/lesson")
    public JsonAPIResponse getPurchasedLessons()
        throws AccessDeniedException, JsonProcessingException, PersistenceException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<LessonDto> purchasedLessons = userService.getPurchasedLessons(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("lessons", purchasedLessons);
        return new APISuccessResponse("Get purchased lessons success.", data, objectMapper);
    }

    @GetMapping("/comments")
    public JsonAPIResponse getMyComments()
            throws JsonProcessingException, PersistenceException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<ReturnCommentDto> myComments = userService.getMyComments(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("comments", myComments);
        return new APISuccessResponse("Get all comments success.", data, objectMapper);
    }

    @GetMapping("/purchasedSheets")
    public JsonAPIResponse getPurchasedSheets()
            throws JsonProcessingException, PersistenceException, AccessDeniedException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetInfoDto> purchasedSheets = userService.getPurchasedSheets(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheets", purchasedSheets);
        return new APISuccessResponse("Get all purchased sheets success.", data, objectMapper);
    }

    @GetMapping("/uploadedSheets")
    public JsonAPIResponse getUploadedSheets()
        throws JsonProcessingException, PersistenceException, AccessDeniedException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetInfoDto> sheetInfoDtos = userService.uploadedSheets(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheets", sheetInfoDtos);
        return new APISuccessResponse("Get all uploaded sheets success.", data, objectMapper);
    }

    @GetMapping("/scrappedSheets")
    public JsonAPIResponse getScrappedSheets()
            throws JsonProcessingException, PersistenceException, AccessDeniedException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetInfoDto> scrappedSheets = userService.getScrappedSheets(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("sheets", scrappedSheets);
        return new APISuccessResponse("Get all scrapped sheets success.", data, objectMapper);
    }

    @GetMapping("/follow")
    public JsonAPIResponse getFolloingFollower()
        throws JsonProcessingException, PersistenceException, AccessDeniedException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<UserProfile> followingFollwer = userService.getFollowingFollwer(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("follower", followingFollwer);
        return new APISuccessResponse("Get all follower success.", data, objectMapper);
    }

    @PostMapping("/update")
    public JsonAPIResponse updateUserInformation(@RequestBody UpdateUserDto userDto)
            throws JsonProcessingException, PersistenceException, AccessDeniedException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        UserProfile userProfile = userService.updateUser(loggedInUser, userDto);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("user", userProfile);
        return new APISuccessResponse("Update user success.", data, objectMapper);
    }

}
