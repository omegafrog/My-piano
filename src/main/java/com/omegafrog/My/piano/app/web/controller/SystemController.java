package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    @GetMapping("/healthcheck")
    public JsonAPIResponse<Void> checkHealth(){
        return new APISuccessResponse<>("hello world");

    }
}
