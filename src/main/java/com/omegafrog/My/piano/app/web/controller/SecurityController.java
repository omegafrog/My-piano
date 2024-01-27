package com.omegafrog.My.piano.app.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.json.gson.GsonFactory;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.utils.DtoMapper;
import com.omegafrog.My.piano.app.utils.response.*;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.security.service.CommonUserService;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import io.awspring.cloud.s3.S3Exception;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SecurityController {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private final CommonUserService commonUserService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GooglePublicKeysManager googlePublicKeysManager;

    @Autowired
    private DtoMapper dtoMapper;


    @GetMapping("/validate")
    public JsonAPIResponse validateToken() {
        return new APISuccessResponse("validate success.");
    }

    @GetMapping("/revalidate")
    public JsonAPIResponse revalidateToken(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        String accessToken = tokenUtils.getAccessTokenStringFromHeaders(request);
        try {
            Claims claims = tokenUtils.extractClaims(accessToken);
        } catch (ExpiredJwtException e) {
            TokenInfo tokenInfo = commonUserService.getTokenInfo(e);
            Map<String, Object> data = ResponseUtil.getStringObjectMap(
                    "access token", tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
            tokenUtils.setRefreshToken(response, tokenInfo);
            return new APISuccessResponse("Token invalidating success.", data);
        }
        return new APIBadRequestResponse("Token is not expired yet.");
    }


    @ExceptionHandler(S3Exception.class)
    public APIBadRequestResponse S3ExceptionHandler(S3Exception ex) {
        return new APIBadRequestResponse(ex.getMessage());
    }

    @PostMapping("/user/register")
    public JsonAPIResponse registerCommonUser(@RequestParam(name = "profileImg") @Nullable MultipartFile profileImg, @RequestParam String registerInfo) throws IOException, UsernameAlreadyExistException {
        RegisterUserDto dto = dtoMapper.parseRegisterUserInfo(registerInfo);
        SecurityUserDto securityUserDto;
        if (profileImg == null)
            securityUserDto = commonUserService.registerUser(dto);
        else
            securityUserDto = commonUserService.registerUser(dto, profileImg);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("user", securityUserDto);
        return new APISuccessResponse("회원가입 성공.", data);
    }



    @ExceptionHandler(AuthenticationException.class)
    public APIForbiddenResponse authenticationErrorResponse(AuthenticationException ex){
        ex.printStackTrace();
        return new APIForbiddenResponse(ex.getMessage());
    }


    @PostMapping("/oauth2/google")
    public JsonAPIResponse registerOrLoginGoogleUser(HttpServletRequest request, HttpServletResponse
            response, @RequestBody String code) throws GeneralSecurityException, IOException, URISyntaxException {
        String parsedCode = objectMapper.readTree(code).get("code").asText();
        GoogleIdToken parsed = GoogleIdToken.parse(GsonFactory.getDefaultInstance(), parsedCode);
        if (!parsed.verify(new GoogleIdTokenVerifier(googlePublicKeysManager)))
            return new APIBadRequestResponse("Google Id token Validation failed.");

        try {
            SecurityUser user = (SecurityUser) commonUserService.loadUserByUsername(parsed.getPayload().getEmail());

            if(!user.isEnabled()){
                if(!user.isAccountNonExpired()) throw new AccountExpiredException("Account is expired. ");
                if(!user.isCredentialsNonExpired())
                    throw new CredentialsExpiredException("Credential is expired at : " +
                            user.getCredentialChangedAt().plusMonths(user.getPasswordExpirationPeriod()));
                if(user.isLocked()) throw new AccountLockedException("Accound is locked.");
            }

            TokenInfo tokenInfo = tokenUtils.generateToken(String.valueOf(user.getId()), user.getRole());
            Optional<RefreshToken> foundedRefreshToken = refreshTokenRepository.findByUserId(user.getId());
            if (foundedRefreshToken.isPresent()) {
                foundedRefreshToken.get().updateRefreshToken(tokenInfo.getRefreshToken().getRefreshToken());
            } else
                refreshTokenRepository.save(tokenInfo.getRefreshToken());
            Map<String, Object> data = ResponseUtil.getStringObjectMap("access token", tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
            tokenUtils.setRefreshToken(response, tokenInfo);
            return new APISuccessResponse("Google OAuth login success.", data);
            // token만들어반환
        } catch (UsernameNotFoundException e) {
            // 회원가입 해야함.
            RegisterUserDto build = RegisterUserDto.builder()
                    .email(parsed.getPayload().getEmail())
                    .username(parsed.getPayload().getEmail())
                    .loginMethod(LoginMethod.GOOGLE)
                    .profileSrc(String.valueOf(parsed.getPayload().get("picture")))
                    .name(String.valueOf(parsed.getPayload().get("name")))
                    .build();
            Map<String, Object> data = ResponseUtil.getStringObjectMap("userInfo", build);
            URI uri = new URI(request.getRequestURL().toString());
            String host = uri.getHost();
            int port = uri.getPort();
            return new APIRedirectResponse("You need to register first.", host + ":" + port + "/user/register", data);
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

