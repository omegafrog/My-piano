package com.omegafrog.My.piano.app.security.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.omegafrog.My.piano.app.security.exception.DuplicatePropertyException;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.dto.user.RegisterUserDto;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;

@ExtendWith(MockitoExtension.class)
class CommonUserRegistrationTest {

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityUserRepository securityUserRepository;

    private CommonUserService commonUserService;

    @BeforeEach
    void setUp() {
        commonUserService = new CommonUserService(passwordEncoder, securityUserRepository, null, null, null, null);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
    }

    @Test
    @DisplayName("동일 username 회원가입이 동시에 들어오면 하나만 저장 시도해야 한다.")
    void concurrentRegisterWithSameUsernameAttemptsSingleSave() throws Exception {
        CountDownLatch firstSaveStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstSave = new CountDownLatch(1);
        when(securityUserRepository.findByUsername("user1")).thenReturn(Optional.empty());
        when(securityUserRepository.findByEmail("user1@email.com")).thenReturn(Optional.empty());
        when(securityUserRepository.save(any(SecurityUser.class))).thenAnswer(invocation -> {
            firstSaveStarted.countDown();
            releaseFirstSave.await(2, TimeUnit.SECONDS);
            return invocation.getArgument(0);
        });

        FutureTask<Void> firstRegister = new FutureTask<>(() -> {
            commonUserService.registerUserWithoutProfile(registerDto("user1", "user1@email.com"));
            return null;
        });
        Thread firstThread = new Thread(firstRegister);
        firstThread.start();
        assertThat(firstSaveStarted.await(2, TimeUnit.SECONDS)).isTrue();

        assertThatThrownBy(() -> commonUserService.registerUserWithoutProfile(registerDto("user1", "user1@email.com")))
                .isInstanceOf(DuplicatePropertyException.class)
                .hasMessage("중복된 ID가 존재합니다.");

        releaseFirstSave.countDown();
        firstRegister.get(2, TimeUnit.SECONDS);
        verify(securityUserRepository, times(1)).save(any(SecurityUser.class));
    }

    @Test
    @DisplayName("DB unique 제약 위반은 중복 ID 예외로 변환해야 한다.")
    void dataIntegrityViolationIsConvertedToDuplicateUsernameException() {
        SecurityUser existingUser = SecurityUser.builder()
                .username("user1")
                .password("encoded-password")
                .role(com.omegafrog.My.piano.app.web.domain.user.authorities.Role.USER)
                .build();
        when(securityUserRepository.findByUsername("user1")).thenReturn(Optional.empty(), Optional.of(existingUser));
        when(securityUserRepository.findByEmail("user1@email.com")).thenReturn(Optional.empty());
        when(securityUserRepository.save(any(SecurityUser.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate username"));

        assertThatThrownBy(() -> commonUserService.registerUserWithoutProfile(registerDto("user1", "user1@email.com")))
                .isInstanceOf(DuplicatePropertyException.class)
                .hasMessage("중복된 ID가 존재합니다.");
    }

    @Test
    @DisplayName("DB email unique 제약 위반은 중복 이메일 예외로 변환해야 한다.")
    void dataIntegrityViolationIsConvertedToDuplicateEmailException() {
        SecurityUser existingUser = SecurityUser.builder()
                .username("other")
                .password("encoded-password")
                .role(com.omegafrog.My.piano.app.web.domain.user.authorities.Role.USER)
                .build();
        when(securityUserRepository.findByUsername("user1")).thenReturn(Optional.empty(), Optional.empty());
        when(securityUserRepository.findByEmail("user1@email.com")).thenReturn(Optional.empty(), Optional.of(existingUser));
        when(securityUserRepository.save(any(SecurityUser.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate email"));

        assertThatThrownBy(() -> commonUserService.registerUserWithoutProfile(registerDto("user1", "user1@email.com")))
                .isInstanceOf(DuplicatePropertyException.class)
                .hasMessage("중복된 이메일이 존재합니다.");
    }

    private RegisterUserDto registerDto(String username, String email) {
        return RegisterUserDto.builder()
                .username(username)
                .password("password")
                .name("user")
                .email(email)
                .loginMethod(LoginMethod.EMAIL)
                .profileSrc("profileSrc")
                .phoneNum("010-1111-2222")
                .build();
    }
}
