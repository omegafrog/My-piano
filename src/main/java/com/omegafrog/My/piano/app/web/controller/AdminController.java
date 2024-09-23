package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import com.omegafrog.My.piano.app.web.dto.admin.AdminDto;
import com.omegafrog.My.piano.app.web.dto.admin.ControlUserDto;
import com.omegafrog.My.piano.app.web.dto.admin.ReturnSessionDto;
import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonListDto;
import com.omegafrog.My.piano.app.web.dto.lesson.SearchLessonFilter;
import com.omegafrog.My.piano.app.web.dto.post.PostListDto;
import com.omegafrog.My.piano.app.web.dto.post.SearchPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SearchSheetPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.dto.user.UserDto;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.admin.AdminUserService;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminUserService adminUserService;


    @PostMapping("/register")
    public JsonAPIResponse<Void> registerAdmin(@Valid @NotNull String username,
                                               @Valid @NotNull String password,
                                               @Valid @NotNull String name,
                                               @Valid @NotNull String email,
                                               @Valid @NotNull Role role) {
        adminUserService.register(username, password, name, email, role);
        return new ApiResponse<>("Success to register admin.");
    }

    @GetMapping()
    public JsonAPIResponse<AdminDto> loadAdminProfile() {
        AdminDto admin = adminUserService.getAdminProfile();
        return new ApiResponse<>("Load admin profile success.", admin);
    }

    @GetMapping("/sessions")
    public JsonAPIResponse<Page<ReturnSessionDto>> getLoggedInUsers(Pageable pageable) {
        Page<ReturnSessionDto> sessions = adminUserService.getLoggedInUsers(pageable);
        return new ApiResponse<>("Load all users success.", sessions);
    }


    @GetMapping("/users")
    public JsonAPIResponse<Page<UserDto>> getAllUsers(
            @NotNull Pageable pageable,
            @Nullable @RequestParam Long id,
            @Nullable @RequestParam String email,
            @Nullable @RequestParam String username,
            @Nullable @RequestParam LoginMethod loginMethod,
            @Nullable @RequestParam String dateStart,
            @Nullable @RequestParam String dateEnd,
            @Nullable @RequestParam Boolean locked) {
        Page<UserDto> users = adminUserService.getAllUsers(pageable, new SearchUserFilter(id, email, username, loginMethod,
                dateStart, dateEnd, locked));
        return new ApiResponse<>("Load all users success.", users);
    }

    @GetMapping("/log-in-users/count")
    public JsonAPIResponse<Long> countLoggedInUsers() {
        Long data = adminUserService.countLoggedInUsers();
        return new ApiResponse<>("Count logged in users success.", data);
    }

    @DeleteMapping("/users")
    public JsonAPIResponse<Void> disconnectLoggedInUser(@RequestParam Long userId, @RequestParam Role role) {
        adminUserService.disconnectLoggedInUser(userId, role);
        return new ApiResponse<>("Disconnect logged in user success.");
    }

    @PostMapping("/users/{id}")
    public JsonAPIResponse controlUser(@PathVariable Long id, @RequestBody ControlUserDto dto) {
        adminUserService.controlUser(id, dto);
        return new ApiResponse("control user " + id + " success.");
    }

    @GetMapping("/posts")
    public JsonAPIResponse<Page<PostListDto>> getPosts(SearchPostFilter filter, Pageable pageable) throws JsonProcessingException {
        Page<PostListDto> page = adminUserService.getAllPosts(filter, pageable);
        return new ApiResponse<>("get posts success.", page);
    }

    @PutMapping("/posts/{id}")
    public JsonAPIResponse<Void> updatePost(@PathVariable Long id, @RequestBody String options) throws JsonProcessingException {
        adminUserService.update(id, options);
        return new ApiResponse<>("disable post success.");
    }

    @DeleteMapping("/posts/{id}")
    public JsonAPIResponse<Void> deletePost(@PathVariable Long id) {
        adminUserService.deletePost(id);
        return new ApiResponse<>("delete post success.");
    }

    @PostMapping("/posts")
    public JsonAPIResponse<Void> writeNoticePost(@RequestBody String body) throws JsonProcessingException {
        SecurityUser loggedInAdmin = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminUserService.writeNotiPost(body, loggedInAdmin);
        return new ApiResponse<>("write notice post success");
    }

    @GetMapping("/sheets")
    public JsonAPIResponse<Page<SheetPostListDto>> getAllSheets(
            @Valid @PageableDefault(size = 30, page = 0) Pageable pageable,
            @Nullable @RequestParam Long id,
            @Nullable @RequestParam String title,
            @Nullable @RequestParam String username,
            @Nullable @RequestParam Long sheetId,
            @Nullable @RequestParam Instrument instrument,
            @Nullable @RequestParam Difficulty difficulty,
            @Nullable @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Nullable @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        Page<SheetPostListDto> data = adminUserService.getAllSheetPosts(pageable,
                new SearchSheetPostFilter(id, title, username, sheetId, instrument, difficulty, startDate, endDate));
        return new ApiResponse<>("Load all users success.", data);
    }

    @PutMapping("/sheets/{id}")
    public JsonAPIResponse<Void> updateSheet(@PathVariable Long id, @RequestBody String options) throws JsonProcessingException {
        adminUserService.updateSheetPost(id, options);
        return new ApiResponse<>("update sheet post success.");
    }

    @DeleteMapping("/sheets/{id}")
    public JsonAPIResponse<Void> deleteSheet(@PathVariable Long id) {
        adminUserService.deleteSheetPost(id);
        return new ApiResponse<>("delete sheet post success.");
    }

    @GetMapping("/lessons")
    public JsonAPIResponse<Page<LessonListDto>> getLessons(
            @Valid @PageableDefault(size = 30, page = 0) Pageable pageable,
            @Nullable @RequestParam Long id,
            @Nullable @RequestParam String title,
            @Nullable @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @Nullable @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) throws JsonProcessingException {
        Page<LessonListDto> data = adminUserService.getAllLessons(pageable,
                new SearchLessonFilter(id, title, startDate, endDate));
        return new ApiResponse("get lessons success.", data);
    }
}
