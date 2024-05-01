package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.sheet.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.UserApplicationService;
import jakarta.persistence.PersistenceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserApplicationService userService;

    @PostMapping("/cash")
    public JsonAPIResponse<Integer> chargeCash(@RequestBody int cash) throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        int data = userService.chargeCash(cash, loggedInUser);
        return new APISuccessResponse<>("Charge cash " + cash + " success.", data);
    }
    @GetMapping("/community/posts")
    public JsonAPIResponse<List<PostDto>> getMyCommunityPosts()
            throws AccessDeniedException, JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<PostDto> data = userService.getMyCommunityPosts(loggedInUser);
        return new APISuccessResponse<>("Get community posts success.", data);
    }

    @GetMapping("/lesson")
    public JsonAPIResponse<List<LessonDto>> getPurchasedLessons()
        throws AccessDeniedException, JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<LessonDto> data = userService.getPurchasedLessons(loggedInUser);
        return new APISuccessResponse<>("Get purchased lessons success.", data);
    }

    @GetMapping("/comments")
    public JsonAPIResponse<List<ReturnCommentDto>> getMyComments()
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<ReturnCommentDto> data = userService.getMyComments(loggedInUser);
        return new APISuccessResponse<>("Get all comments success.", data);
    }

    @GetMapping("/purchasedSheets")
    public JsonAPIResponse<List<SheetPostDto>> getPurchasedSheets()
            throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetPostDto> data = userService.getPurchasedSheets(loggedInUser);
        return new APISuccessResponse<>("Get all purchased sheets success.", data);
    }

    @GetMapping("/uploadedSheets")
    public JsonAPIResponse<Page<SheetPostDto>> getUploadedSheets(Pageable pageable, @RequestParam boolean unPaged)
        throws JsonProcessingException, PersistenceException, AccessDeniedException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        if(unPaged) pageable = Pageable.unpaged();
        Page<SheetPostDto> data = userService.uploadedSheetPost(loggedInUser,pageable);
        return new APISuccessResponse<>("Get all uploaded sheets success.", data);
    }

    @GetMapping("/likedSheets")
    public JsonAPIResponse<List<SheetPost>> getLikedSheets() throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetPost> data = userService.getLikedSheets(loggedInUser);
        return new APISuccessResponse<>("Get liked sheet post success.", data);
    }

    @GetMapping("/scrappedSheets")
    public JsonAPIResponse<List<SheetInfoDto>> getScrappedSheets()
            throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetInfoDto> data = userService.getScrappedSheets(loggedInUser);
        return new APISuccessResponse<>("Get all scrapped sheets success.", data);
    }

    @GetMapping("/follow")
    public JsonAPIResponse<List<UserInfo>> getFollowingFollower()
        throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<UserInfo> data = userService.getFollowingFollower(loggedInUser);
        return new APISuccessResponse<>("Get all follower success.", data);
    }



    @GetMapping("")
    public JsonAPIResponse<UserInfo> getUserInformation() throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        UserInfo data = userService.getUserProfile(loggedInUser);
        return new APISuccessResponse<>("Get user profile success.", data);
    }

    @PostMapping(value = "")
    public JsonAPIResponse<UserInfo> changeUserInfo(@Valid @RequestParam(name = "updateInfo") String dto, @RequestParam(name = "profileImg") @Nullable MultipartFile profileImg) throws IOException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        UserInfo data = userService.changeUserInfo(dto, loggedInUser, profileImg);
        return new APISuccessResponse<>("Update User profile success.", data);
    }

}
