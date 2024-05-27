package com.omegafrog.My.piano.app.web.service.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.google.api.client.json.gson.GsonFactory;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.user.SecurityUserDto;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.exception.DuplicatePropertyException;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import io.awspring.cloud.s3.ObjectMetadata;
import io.awspring.cloud.s3.S3Template;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
import software.amazon.awssdk.services.s3.model.PutObjectAclRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class CommonUserService implements UserDetailsService {

    @Lazy
    private final PasswordEncoder passwordEncoder;
    private final SecurityUserRepository securityUserRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;
    private final GooglePublicKeysManager googlePublicKeysManager;
    @Autowired
    private S3Template s3Template;
    @Value("${spring.cloud.aws.bucket.name}")
    private String bucketName;
    @Value("${spring.cloud.aws.region.static}")
    private String regionName;
    @Value("${security.jwt.secret}")
    private String jwtSecret;
    private final S3Client s3Client;
    @Autowired
    private TokenUtils tokenUtils;


    /**
     * Username으로 SecurityUser 객체를 검색해 반환하는 메소드
     *
     * @param username the username identifying the user whose data is required.
     * @return username을 가지는 UserDetails를 상속하는 SecurityUser 객체를 반환한다.
     * @throws UsernameNotFoundException:파라미터로 주어진 username을 가진 user엔티티가 없으면 throw한다
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<SecurityUser> founded = securityUserRepository.findByUsername(username);
        if(founded.isPresent()){
            return founded.get();
        }else{
            throw new UsernameNotFoundException("No such user.");
        }
    }

    public SecurityUserDto registerUser(GoogleIdToken token){
        SecurityUser securityUser = SecurityUser.builder()
                .username(token.getPayload().getEmail())
                .role(Role.USER)
                .user(User.builder()
                        .email(token.getPayload().getEmail())
                        .name((String) token.getPayload().get("name"))
                        .loginMethod(LoginMethod.GOOGLE)
                        .profileSrc((String) token.getPayload().get("picture"))
                        .cart(new Cart())
                        .build()
                )
                .build();
        SecurityUser saved = securityUserRepository.save(securityUser);
        return saved.toDto();
    }

    public SecurityUserDto registerUserWithoutProfile(RegisterUserDto dto) throws DuplicatePropertyException {
        User user = dto.toEntity();
        SecurityUser.SecurityUserBuilder builder = SecurityUser.builder()
                .username(dto.getUsername())
                .role(Role.USER)
                .user(user);
        if (dto.getLoginMethod().equals(LoginMethod.EMAIL) && dto.getPassword()==null)
            throw new IllegalArgumentException("비밀번호는 비어있지 않아야 합니다.");
        if(dto.getPassword() == null) dto.setPassword("");
        SecurityUser securityUser = builder.password(passwordEncoder.encode(dto.getPassword())).build();
        Optional<SecurityUser> founded = securityUserRepository.findByUsername(securityUser.getUsername());
        Optional<SecurityUser> byEmail = securityUserRepository.findByEmail(securityUser.getUser().getEmail());
        if (founded.isPresent())
            throw new DuplicatePropertyException("중복된 ID가 존재합니다.");
        if(byEmail.isPresent())
            throw new DuplicatePropertyException("중복된 이메일이 존재합니다.");

        SecurityUser saved = securityUserRepository.save(securityUser);
        return saved.toDto();
    }

    public SecurityUserDto registerUser(RegisterUserDto dto, MultipartFile profileImg) throws DuplicatePropertyException, IOException {
        List<String> nameList = Arrays.asList(profileImg.getOriginalFilename().split("\\."));
        if (nameList.size() <2) throw new IllegalArgumentException("잘못된 파일 형식입니다.");

        MediaType mediaType = MediaType.valueOf(profileImg.getContentType());
        if(!(mediaType.equals(MediaType.IMAGE_JPEG) || mediaType.equals(MediaType.IMAGE_PNG)))
                throw new IllegalArgumentException("잘못된 파일 형식입니다.");

        String profileSrc = "https://"+bucketName+".s3."+regionName+".amazonaws.com/"+profileImg.getOriginalFilename();
        dto.setProfileSrc(profileSrc);
        SecurityUserDto securityUserDto = registerUserWithoutProfile(dto);


        s3Template.upload(bucketName,profileImg.getOriginalFilename() , profileImg.getInputStream(), ObjectMetadata.builder()
                .contentType(mediaType.toString()).build());
        s3Client.putObjectAcl(PutObjectAclRequest.builder()
                .bucket(bucketName)
                .key(profileImg.getOriginalFilename())
                .acl(ObjectCannedACL.PUBLIC_READ).build());
        return securityUserDto;
    }


    public void signOutUser(String username){
        SecurityUser securityUser = securityUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("그런 유저는 없습니다."));
        securityUserRepository.deleteById(securityUser.getId());
    }


    public TokenInfo getTokenInfo(ExpiredJwtException e) {
        Long userId = Long.valueOf((String) e.getClaims().get("id"));
        Optional<SecurityUser> founded = securityUserRepository.findById(userId);
        if (founded.isEmpty()) throw new AccessDeniedException("Unauthorized access token.");

        RefreshToken refreshToken = refreshTokenRepository.findByRoleAndUserId(userId, founded.get().getRole())
                .orElseThrow(() -> new AccessDeniedException("로그인이 만료되었습니다."));

        TokenInfo tokenInfo = tokenUtils.generateToken(String.valueOf(userId),founded.get().getRole());
        refreshToken.updateRefreshToken(tokenInfo.getRefreshToken().getPayload());
        return tokenInfo;
    }

    public TokenInfo loginGoogleUser(String code  ) throws GeneralSecurityException, IOException {
        GoogleIdToken parsed = getGoogleIdToken(code);
        verifyGoogleIdToken(parsed);

        SecurityUser user = (SecurityUser) loadUserByUsername(parsed.getPayload().getEmail());

        TokenInfo tokenInfo = tokenUtils.generateToken(String.valueOf(user.getId()), user.getRole());
        Optional<RefreshToken> foundedRefreshToken = refreshTokenRepository.findByRoleAndUserId(user.getId(), user.getRole());
        if (foundedRefreshToken.isPresent()) {
            foundedRefreshToken.get().updateRefreshToken(tokenInfo.getRefreshToken().getPayload());
        } else
            refreshTokenRepository.save(tokenInfo.getRefreshToken());
        return tokenInfo;
    }

    public RegisterUserDto parseGoogleUserInfo(String code) throws IOException, GeneralSecurityException {
        GoogleIdToken parsed = getGoogleIdToken(code);
        if (!parsed.verify(new GoogleIdTokenVerifier(googlePublicKeysManager)))
            throw new GeneralSecurityException("Google Id token Validation failed.");

        return RegisterUserDto.builder()
                .email(parsed.getPayload().getEmail())
                .username(parsed.getPayload().getEmail())
                .loginMethod(LoginMethod.GOOGLE)
                .profileSrc(String.valueOf(parsed.getPayload().get("picture")))
                .name(String.valueOf(parsed.getPayload().get("name")))
                .build();
    }
    public SecurityUser findGoogleUser(String code) throws IOException, GeneralSecurityException {
        GoogleIdToken googleIdToken = getGoogleIdToken(code);
        verifyGoogleIdToken(googleIdToken);
        return (SecurityUser) loadUserByUsername(googleIdToken.getPayload().getEmail());
    }
    private void verifyGoogleIdToken(GoogleIdToken parsed) throws GeneralSecurityException, IOException {
        if (!parsed.verify(new GoogleIdTokenVerifier(googlePublicKeysManager)))
            throw new GeneralSecurityException("Google Id token Validation failed.");
    }

    private GoogleIdToken getGoogleIdToken(String code) throws IOException {
        String parsedCode = objectMapper.readTree(code).get("code").asText();
        GoogleIdToken parsed = GoogleIdToken.parse(GsonFactory.getDefaultInstance(), parsedCode);
        return parsed;
    }

}
