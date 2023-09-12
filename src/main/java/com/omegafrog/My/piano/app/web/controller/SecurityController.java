package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.utils.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.utils.response.APIInternalServerResponse;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SecurityController {

    @Autowired
    private ObjectMapper objectMapper;

    private final CommonUserService commonUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/user/register")
    public JsonAPIResponse registerCommonUser(@RequestBody RegisterUserDto dto){
        try {
            SecurityUserDto securityUserDto = commonUserService.registerUser(dto);
            Map<String, Object> body = new HashMap<>();
            body.put("user", securityUserDto);
            return new APISuccessResponse("회원가입 성공.", body, objectMapper);
        }catch (UsernameAlreadyExistException e){
            //TODO : Username중복 exception 처리
            log.debug("Username 중복됨");
            return new APIBadRequestResponse("중복된 Username입니다.");
        } catch (JsonProcessingException e) {
            //TODO : ObjectMapping이 실패할 경우 Internal server error를 응답해야 한다.
            log.debug("objectmapping 실패함");
            return new APIInternalServerResponse("Object mapping이 실패했습니다.");
        }
    }

    @GetMapping("/user/signOut")
    public JsonAPIResponse signOutUser(Authentication auth){
        SecurityUser user = (SecurityUser) auth.getPrincipal();
        commonUserService.signOutUser(user.getUsername());
        return new APISuccessResponse("회원탈퇴 성공.");
    }

//    @PostMapping("/user/checkPassword")
//    public JsonAPIResponse validateCurrentPassword(@RequestBody String password, Authentication authentication) {
//        String currentPassword = (String) authentication.getCredentials();
//        if(passwordEncoder.matches(password, currentPassword)){
//            return new APISuccessResponse("password 재인증 성공");
//        }else {
//            return new APISuccessResponse("password 재인증 실패");
//        }
//    }

    @GetMapping("/user/someMethod")
    public String someMethod(){
        return "hi";
    }

}
