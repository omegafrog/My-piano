package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.web.dto.lesson.LessonListDto;
import com.omegafrog.My.piano.app.web.dto.lesson.SearchLessonFilter;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Instrument;
import com.omegafrog.My.piano.app.web.service.admin.AdminUserService;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.dto.AdminDto;
import com.omegafrog.My.piano.app.web.dto.admin.ControlUserDto;
import com.omegafrog.My.piano.app.web.dto.ReturnSessionDto;
import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import com.omegafrog.My.piano.app.web.dto.post.PostListDto;
import com.omegafrog.My.piano.app.web.dto.post.SearchPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SearchSheetPostFilter;
import com.omegafrog.My.piano.app.web.dto.sheetPost.SheetPostListDto;
import com.omegafrog.My.piano.app.web.dto.user.UserDto;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private final MapperUtil mapperUtil;


    @PostMapping("/register")
    public JsonAPIResponse registerAdmin( String password, String username, String name, String email, Role role) {
        adminUserService.register(username, password, name, email, role);
        return new APISuccessResponse("Success to register admin.");
    }

    @GetMapping()
    public JsonAPIResponse loadAdminProfile() throws JsonProcessingException {
        SecurityUser admin =(SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        AdminDto data = new AdminDto(admin.getId(), admin.getUser().getName(), admin.getUser().getEmail(), admin.getRole());
        return new APISuccessResponse("Load admin profile success.", data);
    }

    @GetMapping("/sessions")
    public JsonAPIResponse getLoggedInUsers(Pageable pageable) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        List<ReturnSessionDto> sessions = adminUserService.getLoggedInUsers(pageable);
        data.put("sessions", sessions);
        data.put("count", adminUserService.countLoggedInUsers());
        return new APISuccessResponse("Load all users success.", data);
    }


    @GetMapping("/users")
    public JsonAPIResponse< Map<String, Object>> getAllUsers(Pageable pageable,
                                                      @Nullable @RequestParam Long id,
                                                      @Nullable @RequestParam String email,
                                                      @Nullable @RequestParam String username,
                                                      @Nullable @RequestParam LoginMethod loginMethod,
                                                      @Nullable @RequestParam
                                                      String dateStart,
                                                      @Nullable @RequestParam String dateEnd,
                                                      @Nullable @RequestParam Boolean locked) throws JsonProcessingException {
        Map<String, Object> data = new HashMap<>();
        List<UserDto> users = adminUserService.getAllUsers(pageable, new SearchUserFilter(id, email, username, loginMethod,
                dateStart, dateEnd, locked));
        data.put("users", users);
        data.put("count", adminUserService.countAllUsers());
        return new APISuccessResponse<>("Load all users success.", data);
    }
    @GetMapping("/log-in-users/count")
    public JsonAPIResponse<Long> countLoggedInUsers() throws JsonProcessingException {
        Long data = adminUserService.countLoggedInUsers();
        return new APISuccessResponse<>("Count logged in users success.", data);
    }

    @DeleteMapping("/users")
    public JsonAPIResponse<Void> disconnectLoggedInUser(@RequestParam Long userId, @RequestParam Role role) {
        adminUserService.disconnectLoggedInUser(userId, role);
        return new APISuccessResponse<>("Disconnect logged in user success.");
    }

    @PostMapping("/users/{id}")
    public JsonAPIResponse controlUser(@PathVariable Long id, @RequestBody ControlUserDto dto) {

        adminUserService.controlUser(id, dto);
        return new APISuccessResponse("control user " + id + " success.");
    }

    @GetMapping("/posts")
    public JsonAPIResponse<Page<PostListDto>> getPosts(SearchPostFilter filter, Pageable pageable) throws JsonProcessingException {
        Page<PostListDto> page = adminUserService.getAllPosts(filter, pageable);
        return new APISuccessResponse<>("get posts success.", page);
    }

    @PutMapping("/posts/{id}")
    public JsonAPIResponse<Void> updatePost(@PathVariable Long id, @RequestBody String options) throws JsonProcessingException {
        adminUserService.update(id, options);
        return new APISuccessResponse<>("disable post success.");
    }
    @DeleteMapping("/posts/{id}")
    public JsonAPIResponse<Void> deletePost(@PathVariable Long id) {
        adminUserService.deletePost(id);
        return new APISuccessResponse<>("delete post success.");
    }

    @PostMapping("/posts")
    public JsonAPIResponse<Void> writeNoticePost(@RequestBody String body) throws JsonProcessingException {
        SecurityUser loggedInAdmin =(SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        adminUserService.writeNotiPost(body, loggedInAdmin);
        return new APISuccessResponse<>("write notice post success");
    }

    @GetMapping("/sheets")
    public JsonAPIResponse< Page<SheetPostListDto>> getAllSheets(Pageable pageable,
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
        return new APISuccessResponse<>("Load all users success.", data);
    }
    @PutMapping("/sheets/{id}")
    public JsonAPIResponse<Void> updateSheet(@PathVariable Long id, @RequestBody String options) throws JsonProcessingException {
        adminUserService.updateSheetPost(id, options);
        return new APISuccessResponse<>("update sheet post success.");
    }
    @DeleteMapping("/sheets/{id}")
    public JsonAPIResponse<Void> deleteSheet(@PathVariable Long id){
        adminUserService.deleteSheetPost(id);
        return new APISuccessResponse<>("delete sheet post success.");
    }
    @GetMapping("/lessons")
    public JsonAPIResponse<Page<LessonListDto>> getLessons(
            Pageable pageable,
            @Nullable @RequestParam Long id,
            @Nullable @RequestParam String title,
            @Nullable @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate startDate,
            @Nullable @RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate endDate
    ) throws JsonProcessingException {
        Page<LessonListDto> data = adminUserService.getAllLessons(pageable,
                new SearchLessonFilter(id, title, startDate, endDate));
        return new APISuccessResponse("get lessons success.", data);
    }
}
