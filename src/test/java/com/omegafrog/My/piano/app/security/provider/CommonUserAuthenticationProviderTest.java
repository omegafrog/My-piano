package com.omegafrog.My.piano.app.security.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.omegafrog.My.piano.app.web.domain.cart.Cart;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import com.omegafrog.My.piano.app.web.vo.user.LoginMethod;

@ExtendWith(MockitoExtension.class)
class CommonUserAuthenticationProviderTest {

    @Mock
    private CommonUserService commonUserService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인 인증 성공 시 Authentication details에는 User를 담아야 한다.")
    void authenticatedTokenDetailsContainsUser() {
        User user = user();
        SecurityUser securityUser = SecurityUser.builder()
                .username("user1")
                .password("encoded-password")
                .role(Role.USER)
                .user(user)
                .build();
        CommonUserAuthenticationProvider provider = new CommonUserAuthenticationProvider(commonUserService, passwordEncoder);

        given(commonUserService.loadUserByUsername("user1")).willReturn(securityUser);
        given(passwordEncoder.matches("password", "encoded-password")).willReturn(true);

        Authentication authentication = provider.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated("user1", "password"));

        assertThat(authentication.getPrincipal()).isSameAs(securityUser);
        assertThat(authentication.getDetails()).isSameAs(user);
    }

    private User user() {
        return User.builder()
                .name("user")
                .email("user@email.com")
                .loginMethod(LoginMethod.EMAIL)
                .cart(new Cart())
                .profileSrc("profileSrc")
                .build();
    }
}
