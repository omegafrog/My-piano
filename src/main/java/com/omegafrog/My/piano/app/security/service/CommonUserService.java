package com.omegafrog.My.piano.app.security.service;

import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.omegafrog.My.piano.app.security.exception.AlreadyRegisteredUserException;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenInfo;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.utils.response.APISuccessResponse;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.RegisterUserDto;
import com.omegafrog.My.piano.app.web.dto.SecurityUserDto;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.exception.UsernameAlreadyExistException;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class CommonUserService implements UserDetailsService {

    @Lazy
    private final PasswordEncoder passwordEncoder;

    private final SecurityUserRepository securityUserRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private  UserRepository userRepository;


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
        SecurityUser securityUser = builder.password(dto.getPassword()).build();
        Optional<SecurityUser> founded = securityUserRepository.findByUsername(securityUser.getUsername());
        if (founded.isEmpty()) {
            SecurityUser saved = securityUserRepository.save(securityUser);
            return saved.toDto();
        } else {
            throw new UsernameAlreadyExistException("중복된 ID가 존재합니다.");
        }
    }
    public void signOutUser(String username){
        SecurityUser securityUser = securityUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("그런 유저는 없습니다."));
        securityUserRepository.deleteById(securityUser.getId());
    }
}
