package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    @GetMapping("/healthcheck")
    public JsonAPISuccessResponse<Void> checkHealth(){
        return new ApiSuccessResponse<>("hello world");

    }
}
