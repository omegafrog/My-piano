package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonListDto;
import com.omegafrog.My.piano.app.web.dto.lesson.SearchLessonFilter;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.service.admin.AdminUserService;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.dto.admin.AdminDto;
import com.omegafrog.My.piano.app.web.dto.admin.ControlUserDto;
import com.omegafrog.My.piano.app.web.dto.admin.ReturnSessionDto;
import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import com.omegafrog.My.piano.app.web.dto.post.PostListDto;
import com.omegafrog.My.piano.app.web.dto.post.SearchPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SearchSheetPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.dto.user.UserDto;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminUserService adminUserService;


    @PostMapping("/register")
    public JsonAPISuccessResponse<Void> registerAdmin(@Valid @NotNull String username,
                                                @Valid @NotNull String password,
                                                @Valid @NotNull String name,
                                                @Valid @NotNull String email,
                                                @Valid @NotNull Role role) {
        adminUserService.register(username, password, name, email, role);
        return new ApiSuccessResponse<>("Success to register admin.");
    }

    @GetMapping()
    public JsonAPISuccessResponse<AdminDto> loadAdminProfile() {
        User admin = AuthenticationUtil.getLoggedInUser();
        AdminDto adminDto = new AdminDto(admin.getId(),
                admin.getName(), admin.getEmail(), admin.getSecurityUser().getRole());
        return new ApiSuccessResponse<>("Load admin profile success.", adminDto);
    }

    @GetMapping("/sessions")
    public JsonAPISuccessResponse<Page<ReturnSessionDto>> getLoggedInUsers(Pageable pageable) {
        Page<ReturnSessionDto> sessions = adminUserService.getLoggedInUsers(pageable);
        return new ApiSuccessResponse<>("Load all users success.",sessions);
    }


    @GetMapping("/users")
    public JsonAPISuccessResponse<Page<UserDto>> getAllUsers(@NotNull Pageable pageable,
                                                                    @Nullable @RequestParam Long id,
                                                                    @Nullable @RequestParam String email,
                                                                    @Nullable @RequestParam String username,
                                                                    @Nullable @RequestParam LoginMethod loginMethod,
                                                                    @Nullable @RequestParam String dateStart,
                                                                    @Nullable @RequestParam String dateEnd,
                                                                    @Nullable @RequestParam Boolean locked){
        Page<UserDto> users = adminUserService.getAllUsers(pageable, new SearchUserFilter(id, email, username, loginMethod,
                dateStart, dateEnd, locked));
        return new ApiSuccessResponse<>("Load all users success.", users);
    }
    @GetMapping("/log-in-users/count")
    public JsonAPISuccessResponse<Long> countLoggedInUsers(){
        Long data = adminUserService.countLoggedInUsers();
        return new ApiSuccessResponse<>("Count logged in users success.", data);
    }

    @DeleteMapping("/users")
    public JsonAPISuccessResponse<Void> disconnectLoggedInUser(@RequestParam Long userId, @RequestParam Role role) {
        adminUserService.disconnectLoggedInUser(userId, role);
        return new ApiSuccessResponse<>("Disconnect logged in user success.");
    }

    @PostMapping("/users/{id}")
    public JsonAPISuccessResponse controlUser(@PathVariable Long id, @RequestBody ControlUserDto dto) {

        adminUserService.controlUser(id, dto);
        return new ApiSuccessResponse("control user " + id + " success.");
    }

    @GetMapping("/posts")
    public JsonAPISuccessResponse<Page<PostListDto>> getPosts(SearchPostFilter filter, Pageable pageable) throws JsonProcessingException {
        Page<PostListDto> page = adminUserService.getAllPosts(filter, pageable);
        return new ApiSuccessResponse<>("get posts success.", page);
    }

    @PutMapping("/posts/{id}")
    public JsonAPISuccessResponse<Void> updatePost(@PathVariable Long id, @RequestBody String options) throws JsonProcessingException {
        adminUserService.update(id, options);
        return new ApiSuccessResponse<>("disable post success.");
    }
    @DeleteMapping("/posts/{id}")
    public JsonAPISuccessResponse<Void> deletePost(@PathVariable Long id) {
        adminUserService.deletePost(id);
        return new ApiSuccessResponse<>("delete post success.");
    }

    @PostMapping("/posts")
    public JsonAPISuccessResponse<Void> writeNoticePost(@RequestBody String body) throws JsonProcessingException {
        SecurityUser loggedInAdmin =(SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminUserService.writeNotiPost(body, loggedInAdmin);
        return new ApiSuccessResponse<>("write notice post success");
    }

    @GetMapping("/sheets")
    public JsonAPISuccessResponse< Page<SheetPostListDto>> getAllSheets(Pageable pageable,
                                                                        @Nullable @RequestParam Long id,
                                                                        @Nullable @RequestParam String title,
                                                                        @Nullable @RequestParam String username,
                                                                        @Nullable @RequestParam Long sheetId,
                                                                        @Nullable @RequestParam Instrument instrument,
                                                                        @Nullable @RequestParam Difficulty difficulty,
                                                                        @Nullable @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                                     LocalDate startDate,
                                                                        @Nullable @RequestParam  @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                                     LocalDate endDate
                                                          ) throws JsonProcessingException {
        Page<SheetPostListDto> data = adminUserService.getAllSheetPosts(pageable,
                new SearchSheetPostFilter(id, title, username, sheetId, instrument, difficulty, startDate, endDate));
        return new ApiSuccessResponse<>("Load all users success.", data);
    }
    @PutMapping("/sheets/{id}")
    public JsonAPISuccessResponse<Void> updateSheet(@PathVariable Long id, @RequestBody String options) throws JsonProcessingException {
        adminUserService.updateSheetPost(id, options);
        return new ApiSuccessResponse<>("update sheet post success.");
    }
    @DeleteMapping("/sheets/{id}")
    public JsonAPISuccessResponse<Void> deleteSheet(@PathVariable Long id){
        adminUserService.deleteSheetPost(id);
        return new ApiSuccessResponse<>("delete sheet post success.");
    }
    @GetMapping("/lessons")
    public JsonAPISuccessResponse<Page<LessonListDto>> getLessons(
            Pageable pageable,
            @Nullable @RequestParam Long id,
            @Nullable @RequestParam String title,
            @Nullable @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate startDate,
            @Nullable @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate endDate
    ) throws JsonProcessingException {
        Page<LessonListDto> data = adminUserService.getAllLessons(pageable,
                new SearchLessonFilter(id, title, startDate, endDate));
        return new ApiSuccessResponse("get lessons success.", data);
    }
}
