package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.comment.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name="유저 컨트롤러", description = "유저 API 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserApplicationService userService;

    @GetMapping("/posts")
    @Operation(summary = "작성글 조회", description = "유저가 작성한 커뮤니티 글을 조회한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200")
    })
    public JsonAPISuccessResponse<List<PostDto>> getMyCommunityPosts()
            throws AccessDeniedException, JsonProcessingException {

        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<PostDto> data = userService.getMyCommunityPosts(loggedInUser);
        return new ApiSuccessResponse<>("Get community posts success.", data);
    }

    @GetMapping("/lesson")
    public JsonAPISuccessResponse<List<LessonDto>> getPurchasedLessons()
        throws AccessDeniedException, JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<LessonDto> data = userService.getPurchasedLessons(loggedInUser);
        return new ApiSuccessResponse<>("Get purchased lessons success.", data);
    }

    @GetMapping("/comments")
    public JsonAPISuccessResponse<List<ReturnCommentDto>> getMyComments()
            throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<ReturnCommentDto> data = userService.getMyComments(loggedInUser);
        return new ApiSuccessResponse<>("Get all comments success.", data);
    }

    @GetMapping("/purchasedSheets")
    public JsonAPISuccessResponse<List<SheetPostDto>> getPurchasedSheets()
            throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetPostDto> data = userService.getPurchasedSheets(loggedInUser);
        return new ApiSuccessResponse<>("Get all purchased sheets success.", data);
    }

    @GetMapping("/uploadedSheets")
    public JsonAPISuccessResponse<Page<SheetPostDto>> getUploadedSheets(Pageable pageable, @RequestParam boolean unPaged)
        throws JsonProcessingException, PersistenceException, AccessDeniedException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        if(unPaged) pageable = Pageable.unpaged();
        Page<SheetPostDto> data = userService.uploadedSheetPost(loggedInUser,pageable);
        return new ApiSuccessResponse<>("Get all uploaded sheets success.", data);
    }

    @GetMapping("/likedSheets")
    public JsonAPISuccessResponse<List<SheetPost>> getLikedSheets() throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetPost> data = userService.getLikedSheets(loggedInUser);
        return new ApiSuccessResponse<>("Get liked sheet post success.", data);
    }

    @GetMapping("/scrappedSheets")
    public JsonAPISuccessResponse<List<SheetInfoDto>> getScrappedSheets()
            throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<SheetInfoDto> data = userService.getScrappedSheets(loggedInUser);
        return new ApiSuccessResponse<>("Get all scrapped sheets success.", data);
    }

    @GetMapping("/follow")
    public JsonAPISuccessResponse<List<UserInfo>> getFollowingFollower()
        throws JsonProcessingException{
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        List<UserInfo> data = userService.getFollowingFollower(loggedInUser);
        return new ApiSuccessResponse<>("Get all follower success.", data);
    }



    @GetMapping("")
    public JsonAPISuccessResponse<UserInfo> getUserInformation() throws JsonProcessingException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        UserInfo data = userService.getUserProfile(loggedInUser);
        return new ApiSuccessResponse<>("Get user profile success.", data);
    }

    @PostMapping(value = "")
    public JsonAPISuccessResponse<UserInfo> changeUserInfo(@Valid @RequestParam(name = "updateInfo") String dto, @RequestParam(name = "profileImg") @Nullable MultipartFile profileImg) throws IOException {
        User loggedInUser = AuthenticationUtil.getLoggedInUser();
        UserInfo data = userService.changeUserInfo(dto, loggedInUser, profileImg);
        return new ApiSuccessResponse<>("Update User profile success.", data);
    }

}
