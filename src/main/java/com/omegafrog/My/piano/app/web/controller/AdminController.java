package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.service.AdminUserService;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminUserService adminUserService;

    @PostMapping("/register")
    public JsonAPIResponse registerAdmin(String email, String name, String password, String username, Role role) {
        adminUserService.register(username, password, email, name, role);

        return new APISuccessResponse("Success to register admin.");
    }
}
