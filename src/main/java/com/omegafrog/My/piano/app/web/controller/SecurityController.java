package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.user.SecurityUserDto;
import com.omegafrog.My.piano.app.security.exception.DuplicatePropertyException;
import com.omegafrog.My.piano.app.web.response.APIBadRequestSuccessResponse;
import com.omegafrog.My.piano.app.web.response.APIForbiddenSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.ApiSuccessResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPISuccessResponse;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import io.awspring.cloud.s3.S3Exception;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class SecurityController {

    private final TokenUtils tokenUtils;
    private final CommonUserService commonUserService;
    private final MapperUtil mapperUtil;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;


    @GetMapping("/validate")
    public JsonAPISuccessResponse<Void> validateToken() {
        return new ApiSuccessResponse<>("validate success.");
    }

    @GetMapping("/revalidate")
    public JsonAPISuccessResponse revalidateToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = tokenUtils.getAccessTokenString(request.getHeader(HttpHeaders.AUTHORIZATION));

        try {
            tokenUtils.extractClaims(accessToken);
        }catch (ExpiredJwtException e) {
            TokenInfo tokenInfo = commonUserService.getTokenInfo(e);
            String accessTokenString = tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken();
            tokenUtils.setRefreshToken(response, tokenInfo);
            return new ApiSuccessResponse<>("Token revalidating success.", accessTokenString);
        }
        return new APIBadRequestSuccessResponse("Token is not expired yet.");
    }


    @ExceptionHandler(S3Exception.class)
    public APIBadRequestSuccessResponse S3ExceptionHandler(S3Exception ex) {
        return new APIBadRequestSuccessResponse(ex.getMessage());
    }

    @PostMapping("/user/register")
    public JsonAPISuccessResponse<SecurityUserDto> registerCommonUser(
            @Valid @RequestParam(name = "profileImg") @Nullable MultipartFile profileImg,
            @Valid @NotNull @RequestParam String registerInfo)
            throws IOException, DuplicatePropertyException {
        RegisterUserDto dto = mapperUtil.parseRegisterUserInfo(registerInfo);
        SecurityUserDto securityUserDto;
        if (profileImg == null)
            securityUserDto = commonUserService.registerUserWithoutProfile(dto);
        else
            securityUserDto = commonUserService.registerUser(dto, profileImg);
        return new ApiSuccessResponse<>("회원가입 성공.", securityUserDto);
    }

    @GetMapping("/oauth2/google/callback")
    public ResponseEntity<Map<String, Object>> registerOrLoginGoogleUser(
            HttpServletResponse response,
            @Valid @NotNull @RequestParam("code") String code)
            throws GeneralSecurityException, IOException{

        String idToken = getGoogleIdToken(code);
        Map<String, Object> data = new HashMap<>();
        ResponseEntity<Map<String, Object>> apiRedirectResponse = null;
        try {
            SecurityUser user =  commonUserService.findGoogleUser(idToken);
            validateEnabled(user);

            TokenInfo tokenInfo = commonUserService.loginGoogleUser(idToken );
            tokenUtils.setRefreshToken(response, tokenInfo);
            data.put("status", 302);
            data.put("message", "Google OAuth login success.");
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.put(HttpHeaders.LOCATION, List.of("http://localhost:3000/oauth2/google/login?access-token="
                    + tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken()));
            apiRedirectResponse = new ResponseEntity<>(data,headers, HttpStatus.TEMPORARY_REDIRECT);
        } catch (UsernameNotFoundException e) {
            data.put("status", 302);
            MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
            headers.put(HttpHeaders.LOCATION, List.of("http://localhost:3000/user/register?id-token="+idToken));
            apiRedirectResponse = new ResponseEntity<>(data,headers, HttpStatus.TEMPORARY_REDIRECT);
        }
        return apiRedirectResponse;
    }

    private String getGoogleIdToken(String code) throws IOException {
        GoogleTokenResponse executed = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(), new GsonFactory(), clientId, clientSecret, code,redirectUri)
                .execute();
        String idToken= executed.getIdToken();
        return idToken;
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
    public JsonAPISuccessResponse signOutUser(Authentication auth) {
        SecurityUser user = (SecurityUser) auth.getPrincipal();
        commonUserService.signOutUser(user.getUsername());
        return new ApiSuccessResponse("회원탈퇴 성공.");
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

