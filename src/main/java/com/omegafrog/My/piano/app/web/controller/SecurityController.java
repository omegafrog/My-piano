package com.omegafrog.My.piano.app.web.controller;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.omegafrog.My.piano.app.security.exception.DuplicatePropertyException;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.user.SecurityUserDto;
import com.omegafrog.My.piano.app.web.response.APIBadRequestResponse;
import com.omegafrog.My.piano.app.web.response.success.APIRedirectResponse;
import com.omegafrog.My.piano.app.web.response.success.ApiResponse;
import com.omegafrog.My.piano.app.web.response.success.JsonAPIResponse;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import io.awspring.cloud.s3.S3Exception;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import java.io.IOException;
import java.net.URI;
import java.security.GeneralSecurityException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@Slf4j
public class SecurityController {

    private final TokenUtils tokenUtils;
    private final CommonUserService commonUserService;
    private final MapperUtil mapperUtil;
    private final AuthenticationUtil authenticationUtil;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;


    private String GOOGLE_LOGIN_URI = "/oauth2/google/login";
    private String GOOGLE_REGISTER_URI = "/user/register";

    @GetMapping("/validate")
    public JsonAPIResponse<Void> validateToken() {
        return new ApiResponse<>("validate success.");
    }

    @GetMapping("/revalidate")
    public JsonAPIResponse revalidateToken(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = tokenUtils.getAccessTokenString(request.getHeader(HttpHeaders.AUTHORIZATION));

        try {
            tokenUtils.extractClaims(accessToken);
        } catch (ExpiredJwtException e) {
            TokenInfo tokenInfo = commonUserService.getTokenInfo(e);
            String accessTokenString = tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken();
            tokenUtils.setRefreshToken(response, tokenInfo);
            return new ApiResponse<>("Token revalidating success.", accessTokenString);
        }
        return new APIBadRequestResponse("Token is not expired yet.");
    }


    @ExceptionHandler(S3Exception.class)
    public APIBadRequestResponse S3ExceptionHandler(S3Exception ex) {
        return new APIBadRequestResponse(ex.getMessage());
    }

    @PostMapping("/user/register")
    public JsonAPIResponse<SecurityUserDto> registerCommonUser(
            @Valid @RequestPart(name = "profileImg") @Nullable MultipartFile profileImg,
            @Valid @NotNull @RequestPart(name = "registerInfo") String registerInfo)
            throws IOException, DuplicatePropertyException {
        RegisterUserDto dto = mapperUtil.parseRegisterUserInfo(registerInfo);
        SecurityUserDto securityUserDto;
        if (profileImg == null)
            securityUserDto = commonUserService.registerUserWithoutProfile(dto);
        else
            securityUserDto = commonUserService.registerUser(dto, profileImg);
        return new ApiResponse<>("회원가입 성공.", securityUserDto);
    }

    @GetMapping("/oauth2/google/callback")
    public JsonAPIResponse registerOrLoginGoogleUser(
            HttpServletRequest request, HttpServletResponse response,
            @Valid @NotNull @RequestParam("code") String code) throws IOException {
        String idToken = getGoogleIdToken(code);
        try {
            validateEnabled(commonUserService.findGoogleUser(idToken));

            TokenInfo tokenInfo = commonUserService.loginGoogleUser(idToken);
            tokenUtils.setRefreshToken(response, tokenInfo);
            String param = tokenInfo.getGrantType() + "%20" + tokenInfo.getAccessToken();
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("http://localhost:3000" + GOOGLE_LOGIN_URI
                    + "?access-token=" + param));
            return new APIRedirectResponse<>("Google OAuth login success",
                    headers);
        } catch (UsernameNotFoundException e) {
            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create("http://" + request.getRemoteHost() + ":" + request.getRemotePort() + GOOGLE_REGISTER_URI
                    + "?id-token=" + idToken));
            return new APIRedirectResponse("Register Google User",
                    headers);
        } catch (GeneralSecurityException e) {
            throw new AccessDeniedException(e.getMessage(), e);
        }
    }

    private String getGoogleIdToken(String code) throws IOException {
        GoogleTokenResponse executed = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(), new GsonFactory(), clientId, clientSecret, code, redirectUri)
                .execute();
        String idToken = executed.getIdToken();
        return idToken;
    }

    private static void validateEnabled(SecurityUser user) throws AccountExpiredException, AccountLockedException {
        if (!user.isEnabled()) {
            if (!user.isAccountNonExpired()) throw new AccountExpiredException("Account is expired. ");
            if (!user.isCredentialsNonExpired())
                throw new CredentialsExpiredException("Credential is expired at : " +
                        user.getCredentialChangedAt().plusMonths(user.getPasswordExpirationPeriod()));
            if (user.isLocked()) throw new AccountLockedException("Accound is locked.");
        }
    }


    @GetMapping("/user/signOut")
    public JsonAPIResponse signOutUser() {
        commonUserService.signOutUser();
        return new ApiResponse("회원탈퇴 성공.");
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

