package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.comment.ReturnCommentDto;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonDto;
import com.omegafrog.My.piano.app.web.dto.post.PostDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetInfoDto;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostDto;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import com.omegafrog.My.piano.app.web.dto.videoPost.VideoPostDto;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.UserApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.List;

@Tag(name = "유저 컨트롤러", description = "유저 API 컨트롤러")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserApplicationService userService;

    @GetMapping("/posts")
    @Operation(summary = "작성글 조회", description = "유저가 작성한 커뮤니티 글을 조회한다.")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200")
    })
    public JsonAPIResponse<List<PostDto>> getMyCommunityPosts() {
        List<PostDto> data = userService.getMyCommunityPosts();
        return new ApiResponse<>("Get community posts success.", data);
    }

    @GetMapping("/purchasedLessons")
    public JsonAPIResponse<List<LessonDto>> getPurchasedLessons() {
        List<LessonDto> data = userService.getPurchasedLessons();
        return new ApiResponse<>("Get purchased lessons success.", data);
    }

    @GetMapping("/comments")
    public JsonAPIResponse<List<ReturnCommentDto>> getMyComments() {
        List<ReturnCommentDto> data = userService.getMyComments();
        return new ApiResponse<>("Get all comments success.", data);
    }

    @GetMapping("/purchasedSheets")
    public JsonAPIResponse<List<SheetPostDto>> getPurchasedSheets() {
        List<SheetPostDto> data = userService.getPurchasedSheets();
        return new ApiResponse<>("Get all purchased sheets success.", data);
    }

    @GetMapping("/uploadedSheets")
    public JsonAPIResponse<Page<SheetPostDto>> getUploadedSheets(
            @Valid @NotNull @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<SheetPostDto> data = userService.uploadedSheetPost(pageable);
        return new ApiResponse<>("Get all uploaded sheets success.", data);
    }

    @GetMapping("/uploaded-video-posts")
    public JsonAPIResponse<Page<VideoPostDto>> getUploadedVideoPosts(
            @Valid @NotNull @PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<VideoPostDto> data = userService.uploadedVideoPost(pageable);
        return new ApiResponse<>("Get all uploaded video posts success.", data);
    }

    @GetMapping("/likedSheets")
    public JsonAPIResponse<List<SheetPost>> getLikedSheets() {
        List<SheetPost> data = userService.getLikedSheets();
        return new ApiResponse<>("Get liked sheet post success.", data);
    }

    @GetMapping("/scrappedSheets")
    public JsonAPIResponse<List<SheetInfoDto>> getScrappedSheets() {
        List<SheetInfoDto> data = userService.getScrappedSheets();
        return new ApiResponse<>("Get all scrapped sheets success.", data);
    }

    @GetMapping("/follow")
    public JsonAPIResponse<List<UserInfo>> getFollowingFollower() {
        List<UserInfo> data = userService.getFollowingFollower();
        return new ApiResponse<>("Get all follower success.", data);
    }

    @GetMapping("")
    public JsonAPIResponse<UserInfo> getUserInformation() {
        UserInfo data = userService.getUserProfile();
        return new ApiResponse<>("Get user profile success.", data);
    }

    @PostMapping(value = "")
    public JsonAPIResponse<UserInfo> changeUserInfo(
            @Valid @NotNull @RequestParam(name = "updateInfo") String dto,
            @Valid @NotNull @RequestParam(name = "profileImg") @Nullable MultipartFile profileImg) throws IOException {
        UserInfo data = userService.changeUserInfo(dto, profileImg);
        return new ApiResponse<>("Update User profile success.", data);
    }
}
