package com.omegafrog.My.piano.app.security.service;

import com.omegafrog.My.piano.app.cart.Cart;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.user.entity.User;
import com.omegafrog.My.piano.app.user.vo.LoginMethod;
import com.omegafrog.My.piano.app.user.vo.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CommonUserServiceTest {

    @Autowired
    private SecurityUserRepository securityUserRepository;

    private final PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder("test", 32,256);


    @Test
    @DisplayName("username으로 User를 조회할 수 있어야 한다.")
    void loadUserByUsername() {
        //given
        SecurityUser securityUser = SecurityUser.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .user(User.builder()
                        .name("name")
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("phoneNum")
                                .isAuthorized(false)
                                .build())
                        .cart(new Cart())
                        .profileSrc("profileSrc")
                        .loginMethod(LoginMethod.EMAIL)
                        .build())
                .build();
        SecurityUser saved = securityUserRepository.save(securityUser);
        //when
        Optional<SecurityUser> founded = securityUserRepository.findByUsername(saved.getUsername());
        //then
        Assertions.assertThat(founded).isPresent();
    }

    @Test
    void registerUser() {
    }
}