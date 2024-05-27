package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.utils.response.*;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.user.SecurityUserDto;
import com.omegafrog.My.piano.app.security.exception.DuplicatePropertyException;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import io.awspring.cloud.s3.S3Exception;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class SecurityController {

    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenUtils tokenUtils;
    private final CommonUserService commonUserService;
    private final MapperUtil mapperUtil;


    @GetMapping("/validate")
    public JsonAPIResponse<Void> validateToken() {
        return new APISuccessResponse<>("validate success.");
    }

    @GetMapping("/revalidate")
    public JsonAPIResponse revalidateToken(HttpServletRequest request, HttpServletResponse response)
            throws JsonProcessingException {
        String accessToken = tokenUtils.getAccessTokenString(request.getHeader(HttpHeaders.AUTHORIZATION));

        try {
            tokenUtils.extractClaims(accessToken);
        }catch (ExpiredJwtException e) {
            TokenInfo tokenInfo = commonUserService.getTokenInfo(e);
            String accessTokenString = tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken();
            tokenUtils.setRefreshToken(response, tokenInfo);
            return new APISuccessResponse<>("Token revalidating success.", accessTokenString);
        }
        return new APIBadRequestResponse("Token is not expired yet.");
    }


    @ExceptionHandler(S3Exception.class)
    public APIBadRequestResponse S3ExceptionHandler(S3Exception ex) {
        return new APIBadRequestResponse(ex.getMessage());
    }

    @PostMapping("/user/register")
    public JsonAPIResponse<SecurityUserDto> registerCommonUser(
            @RequestParam(name = "profileImg") @Nullable MultipartFile profileImg,
            @RequestParam String registerInfo)
            throws IOException, DuplicatePropertyException {
        RegisterUserDto dto = mapperUtil.parseRegisterUserInfo(registerInfo);
        SecurityUserDto securityUserDto;
        if (profileImg == null)
            securityUserDto = commonUserService.registerUserWithoutProfile(dto);
        else
            securityUserDto = commonUserService.registerUser(dto, profileImg);
        return new APISuccessResponse<>("회원가입 성공.", securityUserDto);
    }



    @ExceptionHandler(AuthenticationException.class)
    public APIForbiddenResponse authenticationErrorResponse(AuthenticationException ex){
        ex.printStackTrace();
        return new APIForbiddenResponse(ex.getMessage());
    }

    @PostMapping("/oauth2/google")
    public APIRedirectResponse registerOrLoginGoogleUser(
             HttpServletResponse response, @RequestBody String code)
            throws GeneralSecurityException, IOException{
        try {
            SecurityUser user =  commonUserService.findGoogleUser(code);
            validateEnabled(user);

            TokenInfo tokenInfo = commonUserService.loginGoogleUser(code );
            tokenUtils.setRefreshToken(response, tokenInfo);
            Map<String, Object> data = new HashMap<>();
            data.put("access token", tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
            data.put("redirect_url", "/user/login");
            APIRedirectResponse<Map<String, Object>> stringAPIRedirectResponse = new APIRedirectResponse<>("Google OAuth login success.",
                    "/user/login", data);
            return stringAPIRedirectResponse;
        } catch (UsernameNotFoundException e) {
            RegisterUserDto userDto= commonUserService.parseGoogleUserInfo(code);
Map<String, Object> data = new HashMap<>();
            data.put("redirect_url", "/user/login");
            data.put("registerUserInfo", userDto);
            APIRedirectResponse<Map<String, Object>> apiRedirectResponse = new APIRedirectResponse<>("Google OAuth register success.",
                    "/user/register", data);
            return apiRedirectResponse;
        }
    }

    private static void validateEnabled(SecurityUser user) throws AccountExpiredException, AccountLockedException {
        if(!user.isEnabled()){
            if(!user.isAccountNonExpired()) throw new AccountExpiredException("Account is expired. ");
            if(!user.isCredentialsNonExpired())
                throw new CredentialsExpiredException("Credential is expired at : " +
                        user.getCredentialChangedAt().plusMonths(user.getPasswordExpirationPeriod()));
            if(user.isLocked()) throw new AccountLockedException("Accound is locked.");
        }
    }


    @GetMapping("/user/signOut")
    public JsonAPIResponse signOutUser(Authentication auth) {
        SecurityUser user = (SecurityUser) auth.getPrincipal();
        commonUserService.signOutUser(user.getUsername());
        return new APISuccessResponse("회원탈퇴 성공.");
    }
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

