package com.omegafrog.My.piano.app.web.service.admin;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
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
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
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

    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private S3Template s3Template;
    @Value("${spring.cloud.aws.bucket.name}")
    private String bucketName;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

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

    public SecurityUserDto registerUser(RegisterUserDto dto) throws UsernameAlreadyExistException {
        User user = dto.toEntity();
        SecurityUser.SecurityUserBuilder builder = SecurityUser.builder()
                .username(dto.getUsername())
                .role(Role.USER)
                .user(user);
        if (dto.getLoginMethod().equals(LoginMethod.EMAIL) && dto.getPassword()==null)
            throw new IllegalArgumentException("Password cannot be null.");
        if(dto.getPassword() == null) dto.setPassword("");
        SecurityUser securityUser = builder.password(passwordEncoder.encode(dto.getPassword())).build();
        Optional<SecurityUser> founded = securityUserRepository.findByUsername(securityUser.getUsername());
        if (founded.isEmpty()) {
            SecurityUser saved = securityUserRepository.save(securityUser);
            return saved.toDto();
        } else {
            throw new UsernameAlreadyExistException("중복된 ID가 존재합니다.");
        }
    }

    public SecurityUserDto registerUser(RegisterUserDto dto, MultipartFile profileImg) throws UsernameAlreadyExistException, IOException {
        SecurityUserDto securityUserDto = registerUser(dto);
        List<String> nameList = Arrays.asList(profileImg.getOriginalFilename().split("\\."));
        if (nameList.size() <2) throw new IllegalArgumentException("Wrong image type.");
        String contentType;
        switch (nameList.get(nameList.size()-1)) {
            case "jpg", "jpeg":
                contentType = MediaType.IMAGE_JPEG_VALUE;
                break;
            case "png":
                contentType = MediaType.IMAGE_PNG_VALUE;
                break;
            default:
                throw new IllegalArgumentException("Wrong image type.");
        }

        s3Template.upload(bucketName, profileImg.getOriginalFilename(), profileImg.getInputStream(), ObjectMetadata.builder()
                .contentType(contentType).build());
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

}
