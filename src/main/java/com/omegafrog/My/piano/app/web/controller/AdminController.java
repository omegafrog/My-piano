package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.service.AdminUserService;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.domain.admin.Admin;
import com.omegafrog.My.piano.app.web.dto.AdminDto;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import com.omegafrog.My.piano.app.web.service.UserApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
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
    public  JsonAPIResponse loadAdminProfile() throws JsonProcessingException {
        Admin loggedInAdmin = AuthenticationUtil.getLoggedInAdmin();

        AdminDto adminProfile = adminUserService.getAdminProfile(loggedInAdmin);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("admin", adminProfile);
        return new APISuccessResponse("Load admin profile success.", data);
    }

    @GetMapping("/users")
    public JsonAPIResponse loadUsers(Pageable pageable) throws JsonProcessingException {
        List<UserProfile> users = adminUserService.getUsers(pageable);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("users", users);
        return new APISuccessResponse("Load all users success.", data);
    }
}
