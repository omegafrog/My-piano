package com.omegafrog.My.piano.app.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SystemController {

    @GetMapping("")
    public String sayHello(){
        return "hello";
    }
}
