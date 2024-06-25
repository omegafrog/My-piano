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
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
    public JsonAPISuccessResponse<List<PostDto>> getMyCommunityPosts() {
        List<PostDto> data = userService.getMyCommunityPosts();
        return new ApiSuccessResponse<>("Get community posts success.", data);
    }

    @GetMapping("/lesson")
    public JsonAPISuccessResponse<List<LessonDto>> getPurchasedLessons() {
        List<LessonDto> data = userService.getPurchasedLessons();
        return new ApiSuccessResponse<>("Get purchased lessons success.", data);
    }

    @GetMapping("/comments")
    public JsonAPISuccessResponse<List<ReturnCommentDto>> getMyComments() {
        List<ReturnCommentDto> data = userService.getMyComments();
        return new ApiSuccessResponse<>("Get all comments success.", data);
    }

    @GetMapping("/purchasedSheets")
    public JsonAPISuccessResponse<List<SheetPostDto>> getPurchasedSheets() {
        List<SheetPostDto> data = userService.getPurchasedSheets();
        return new ApiSuccessResponse<>("Get all purchased sheets success.", data);
    }

    @GetMapping("/uploadedSheets")
    public JsonAPISuccessResponse<Page<SheetPostDto>> getUploadedSheets(
            @Valid @NotNull @PageableDefault(size = 10,page = 0) Pageable pageable){
        Page<SheetPostDto> data = userService.uploadedSheetPost(pageable);
        return new ApiSuccessResponse<>("Get all uploaded sheets success.", data);
    }

    @GetMapping("/likedSheets")
    public JsonAPISuccessResponse<List<SheetPost>> getLikedSheets() {
        List<SheetPost> data = userService.getLikedSheets();
        return new ApiSuccessResponse<>("Get liked sheet post success.", data);
    }

    @GetMapping("/scrappedSheets")
    public JsonAPISuccessResponse<List<SheetInfoDto>> getScrappedSheets() {
        List<SheetInfoDto> data = userService.getScrappedSheets();
        return new ApiSuccessResponse<>("Get all scrapped sheets success.", data);
    }

    @GetMapping("/follow")
    public JsonAPISuccessResponse<List<UserInfo>> getFollowingFollower() {
        List<UserInfo> data = userService.getFollowingFollower();
        return new ApiSuccessResponse<>("Get all follower success.", data);
    }

    @GetMapping("")
    public JsonAPISuccessResponse<UserInfo> getUserInformation() {
        UserInfo data = userService.getUserProfile();
        return new ApiSuccessResponse<>("Get user profile success.", data);
    }

    @PostMapping(value = "")
    public JsonAPISuccessResponse<UserInfo> changeUserInfo(
            @Valid @NotNull @RequestParam(name = "updateInfo") String dto,
            @Valid @NotNull @RequestParam(name = "profileImg") @Nullable MultipartFile profileImg) throws IOException {
        UserInfo data = userService.changeUserInfo(dto, profileImg);
        return new ApiSuccessResponse<>("Update User profile success.", data);
    }
}
