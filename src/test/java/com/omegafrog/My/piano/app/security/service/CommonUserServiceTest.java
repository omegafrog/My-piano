package com.omegafrog.My.piano.app.security.service;

import com.omegafrog.My.piano.app.DataJpaTestConfig;
import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;
import com.omegafrog.My.piano.app.web.vo.user.PhoneNum;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

@DataJpaTest
@Import(value = DataJpaTestConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith({SpringExtension.class, MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommonUserServiceTest {

    @Autowired
    private SecurityUserRepository securityUserRepository;

    private final PasswordEncoder passwordEncoder = new Pbkdf2PasswordEncoder("test", 32, 256, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA256);

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
                                .build())
                        .cart(new Cart())
                        .email("email@email.com")
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
    @DisplayName("securityUser를 추가할 수 있어야 한다.")
    void registerUser() {
        SecurityUser securityUser = SecurityUser.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .user(User.builder()
                        .name("name")
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("phoneNum")
                                .build())
                        .cart(new Cart())
                        .profileSrc("profileSrc")
                        .email("email@email.com")
                        .loginMethod(LoginMethod.EMAIL)
                        .build())
                .build();
        SecurityUser saved = securityUserRepository.save(securityUser);
        Optional<SecurityUser> founded = securityUserRepository.findByUsername(saved.getUsername());
        Assertions.assertThat(founded).isPresent();
        Assertions.assertThat(founded.get()).isEqualTo(saved);
    }

    @Test
    @DisplayName("securityUser를 삭제할 수 있어야 한다.")
    void deleteUser() {
        SecurityUser securityUser = SecurityUser.builder()
                .username("username")
                .password(passwordEncoder.encode("password"))
                .user(User.builder()
                        .name("name")
                        .phoneNum(PhoneNum.builder()
                                .phoneNum("phoneNum")
                                .build())
                        .cart(new Cart())
                        .email("email@email.com")
                        .profileSrc("profileSrc")
                        .loginMethod(LoginMethod.EMAIL)
                        .build())
                .build();
        SecurityUser saved = securityUserRepository.save(securityUser);
        securityUserRepository.deleteById(saved.toDto().getId());
        Optional<SecurityUser> founded = securityUserRepository.findById(saved.toDto().getId());
        Assertions.assertThat(founded).isEmpty();

    }
}