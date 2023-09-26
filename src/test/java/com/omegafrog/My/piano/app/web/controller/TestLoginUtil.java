package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;

public class TestLoginUtil {
    public static RegisterUserDto user1 = RegisterUserDto.builder()
            .name("user1")
            .phoneNum("010-1111-2222")
            .profileSrc("src")
            .loginMethod(LoginMethod.EMAIL)
            .email("email1@email.com")
            .username("user1")
            .password("password")
            .build();
    public static RegisterUserDto user2 = RegisterUserDto.builder()
            .name("user2")
            .phoneNum("010-1111-2222")
            .profileSrc("src")
            .email("email2@email.com")
            .loginMethod(LoginMethod.EMAIL)
            .username("user2")
            .password("password")
            .build();

}
