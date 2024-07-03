package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    @GetMapping("/healthcheck")
    public JsonAPIResponse<Void> checkHealth(){
        return new ApiResponse<>("hello world");

    }
}
