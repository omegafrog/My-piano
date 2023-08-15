package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.ResponseKeyName;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.user.UpdateUserDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.UserApplicationService;
import jakarta.persistence.PersistenceException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

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
    public JsonAPIResponse chargeCash(@RequestBody int cash) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        int chargedCash = userService.chargeCash(cash, loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("chargedCash", chargedCash);
        return new APISuccessResponse("Charge cash " + cash + " success.", data, objectMapper);
    }
    @GetMapping("/community/posts")
    public JsonAPIResponse getMyCommunityPosts()
            throws AccessDeniedException, JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<PostDto> myCommunityPosts = userService.getMyCommunityPosts(loggedInUser);
        Map<String, Object> data =
                ResponseUtil.getStringObjectMap(ResponseKeyName.UPLOADED_COMMUNITY_POSTS.keyName, myCommunityPosts);
        return new APISuccessResponse("Get community posts success.", data, objectMapper);
    }

    @GetMapping("/lesson")
    public JsonAPIResponse getPurchasedLessons()
        throws AccessDeniedException, JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<LessonDto> purchasedLessons = userService.getPurchasedLessons(loggedInUser);
        Map<String, Object> data =
                ResponseUtil.getStringObjectMap(ResponseKeyName.PURCHASED_LESSONS.keyName, purchasedLessons);
        return new APISuccessResponse("Get purchased lessons success.", data, objectMapper);
    }

    @GetMapping("/comments")
    public JsonAPIResponse getMyComments()
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<ReturnCommentDto> myComments = userService.getMyComments(loggedInUser);
        Map<String, Object> data =
                ResponseUtil.getStringObjectMap(ResponseKeyName.UPLOADED_COMMENTS.keyName, myComments);
        return new APISuccessResponse("Get all comments success.", data, objectMapper);
    }

    @GetMapping("/purchasedSheets")
    public JsonAPIResponse getPurchasedSheets()
            throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetInfoDto> purchasedSheets = userService.getPurchasedSheets(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap(ResponseKeyName.PURCHASED_SHEETS.keyName, purchasedSheets);
        return new APISuccessResponse("Get all purchased sheets success.", data, objectMapper);
    }

    @GetMapping("/uploadedSheets")
    public JsonAPIResponse getUploadedSheets()
        throws JsonProcessingException, PersistenceException, AccessDeniedException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetInfoDto> sheetInfoDtos = userService.uploadedSheets(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap(ResponseKeyName.UPLOADED_SHEETS.keyName, sheetInfoDtos);
        return new APISuccessResponse("Get all uploaded sheets success.", data, objectMapper);
    }

    @GetMapping("/scrappedSheets")
    public JsonAPIResponse getScrappedSheets()
            throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetInfoDto> scrappedSheets = userService.getScrappedSheets(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap(ResponseKeyName.SCRAPPED_SHEETS.keyName, scrappedSheets);
        return new APISuccessResponse("Get all scrapped sheets success.", data, objectMapper);
    }

    @GetMapping("/follow")
    public JsonAPIResponse getFollowingFollower()
        throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<UserProfile> followingFollower = userService.getFollowingFollower(loggedInUser);
        Map<String, Object> data = ResponseUtil.getStringObjectMap(ResponseKeyName.FOLLOWED_USERS.keyName, followingFollower);
        return new APISuccessResponse("Get all follower success.", data, objectMapper);
    }

    @PostMapping("/update")
    public JsonAPIResponse updateUserInformation(@RequestBody UpdateUserDto userDto)
            throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        UserProfile userProfile = userService.updateUser(loggedInUser, userDto);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("user", userProfile);
        return new APISuccessResponse("Update user success.", data, objectMapper);
    }

}
