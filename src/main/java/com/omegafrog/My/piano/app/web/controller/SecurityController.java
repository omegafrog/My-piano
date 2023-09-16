package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.utils.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.utils.response.ResponseUtil;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.utils.response.JsonAPIResponse;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SecurityController {
    @Autowired
    private SecurityUserRepository securityUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final CommonUserService commonUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${security.jwt.secret}")
    private String secret;

    @GetMapping("/user/login/invalidate")
    public JsonAPIResponse invalidateToken(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        String accessToken = TokenUtils.getAccessTokenStringFromHeaders(request);
        try {
            Claims claims = TokenUtils.extractClaims(accessToken, secret);
        } catch (ExpiredJwtException e) {

            Long userId = Long.valueOf((String) e.getClaims().get("id"));
            Optional<SecurityUser> founded = securityUserRepository.findById(userId);
            if (founded.isEmpty()) throw new AccessDeniedException("Unauthorized access token.");


            TokenInfo tokenInfo = TokenUtils.generateToken(String.valueOf(userId), secret);
            Map<String, Object> data = ResponseUtil.getStringObjectMap(
                    "access token", tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
            TokenUtils.setRefreshToken(response, tokenInfo);

            return new APISuccessResponse("Token invalidating success.", data, objectMapper);
        }
        return new APIBadRequestResponse("Token is not expired yet.");
    }

    @PostMapping("/user/register")
    public JsonAPIResponse registerCommonUser(@Valid @RequestBody RegisterUserDto dto) throws JsonProcessingException, UsernameAlreadyExistException {
        SecurityUserDto securityUserDto = commonUserService.registerUser(dto);
        Map<String, Object> body = new HashMap<>();
        body.put("user", securityUserDto);
        return new APISuccessResponse("회원가입 성공.", body, objectMapper);

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
