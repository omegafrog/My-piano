package com.omegafrog.My.piano.app.security.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.response.APIInternalServerResponse;
import com.omegafrog.My.piano.app.response.APISuccessResponse;
import com.omegafrog.My.piano.app.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SecurityController {

    private final CommonUserService commonUserService;
    @PostMapping("/user/register")
    public JsonAPIResponse registerCommonUser(String username, String password, RegisterUserDto dto){
        try {
            SecurityUserDto securityUserDto = commonUserService.registerUser(username, password, dto);
            Map<String, Object> body = new HashMap<>();
            body.put("user", securityUserDto);
            return new APISuccessResponse("회원가입 성공.", body);
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
}
