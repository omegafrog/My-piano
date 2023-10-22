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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
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
    private SecurityUserRepository securityUserRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final CommonUserService commonUserService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private Environment environment;

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;


    @Autowired
    private GooglePublicKeysManager googlePublicKeysManager;


    @GetMapping("/user/login/validate")
    public JsonAPIResponse validateToken(){
        return new APISuccessResponse("validate success.");
    }

    @GetMapping("/user/login/revalidate")
    public JsonAPIResponse revalidateToken(HttpServletRequest request, HttpServletResponse response) throws JsonProcessingException {
        String accessToken = TokenUtils.getAccessTokenStringFromHeaders(request);
        try {
            Claims claims = TokenUtils.extractClaims(accessToken, secret);
        } catch (ExpiredJwtException e) {

            TokenInfo tokenInfo = getTokenInfo(e);

            Map<String, Object> data = ResponseUtil.getStringObjectMap(
                    "access token", tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
            TokenUtils.setRefreshToken(response, tokenInfo);

            return new APISuccessResponse("Token invalidating success.", data);
        }
        return new APIBadRequestResponse("Token is not expired yet.");
    }

    private TokenInfo getTokenInfo(ExpiredJwtException e) {
        Long userId = Long.valueOf((String) e.getClaims().get("id"));
        Optional<SecurityUser> founded = securityUserRepository.findById(userId);
        if (founded.isEmpty()) throw new AccessDeniedException("Unauthorized access token.");

        RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId).orElseThrow(() -> new AccessDeniedException("로그인이 만료되었습니다."));

        TokenInfo tokenInfo = TokenUtils.generateToken(String.valueOf(userId), secret);
        refreshToken.updateRefreshToken(tokenInfo.getRefreshToken().getRefreshToken());
        return tokenInfo;
    }

    @ExceptionHandler(S3Exception.class)
    public APIBadRequestResponse S3ExceptionHandler(S3Exception ex) {
        return new APIBadRequestResponse(ex.getMessage());
    }

    @PostMapping("/user/register")
    public JsonAPIResponse registerCommonUser(@RequestParam(name = "profileImg") @Nullable MultipartFile profileImg, @RequestParam String registerInfo) throws IOException, UsernameAlreadyExistException {
        RegisterUserDto dto = parseRegisterUserInfo(registerInfo);
        SecurityUserDto securityUserDto;
        if (profileImg == null)
            securityUserDto = commonUserService.registerUser(dto);
        else
            securityUserDto = commonUserService.registerUser(dto, profileImg);
        Map<String, Object> data = ResponseUtil.getStringObjectMap("user", securityUserDto);
        return new APISuccessResponse("회원가입 성공.", data);
    }

    private RegisterUserDto parseRegisterUserInfo(String registerInfo) throws JsonProcessingException {
        JsonNode registerNodeInfo = objectMapper.readTree(registerInfo);
        String username = registerNodeInfo.get("username").asText();
        String password = registerNodeInfo.get("password").asText();
        String name = registerNodeInfo.get("name").asText();
        String email = registerNodeInfo.get("email").asText();
        String phoneNum = registerNodeInfo.get("phoneNum").asText();
        String loginMethod = registerNodeInfo.get("loginMethod").asText();
        String profileSrc = registerNodeInfo.get("profileSrc").asText();
        return RegisterUserDto.builder()
                .username(username)
                .password(password)
                .name(name)
                .email(email)
                .phoneNum(phoneNum)
                .loginMethod(LoginMethod.valueOf(loginMethod))
                .profileSrc(profileSrc)
                .build();
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
            TokenInfo tokenInfo = TokenUtils.generateToken(String.valueOf(user.getId()), secret);
            Optional<RefreshToken> foundedRefreshToken = refreshTokenRepository.findByUserId(user.getId());
            if (foundedRefreshToken.isPresent()) {
                foundedRefreshToken.get().updateRefreshToken(tokenInfo.getRefreshToken().getRefreshToken());
            } else
                refreshTokenRepository.save(tokenInfo.getRefreshToken());
            Map<String, Object> data = ResponseUtil.getStringObjectMap("access token", tokenInfo.getGrantType() + " " + tokenInfo.getAccessToken());
            TokenUtils.setRefreshToken(response, tokenInfo);
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

