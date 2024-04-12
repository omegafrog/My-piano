package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.service.AdminUserService;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.domain.admin.Admin;
import com.omegafrog.My.piano.app.web.dto.AdminDto;
import com.omegafrog.My.piano.app.web.dto.admin.ControlUserDto;
import com.omegafrog.My.piano.app.web.dto.ReturnSessionDto;
import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import com.omegafrog.My.piano.app.web.dto.user.UserDto;
import com.omegafrog.My.piano.app.web.service.UserApplicationService;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserApplicationService userApplicationService;


    @PostMapping("/register")
    public JsonAPIResponse registerAdmin(String email, String name, String password, String username, Role role) {
        adminUserService.register(username, password, email, name, role);

        return new APISuccessResponse("Success to register admin.");
    }

    @GetMapping()
    public JsonAPIResponse loadAdminProfile() throws JsonProcessingException {
        Admin loggedInAdmin = AuthenticationUtil.getLoggedInAdmin();

        AdminDto data = adminUserService.getAdminProfile(loggedInAdmin);
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
        log.info("dto:{}", dto);
        adminUserService.controlUser(id, dto);
        return new APISuccessResponse("control user " + id + " success.");
    }
}
