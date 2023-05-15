package com.omegafrog.My.piano.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.handler.CommonUserLoginFailureHandler;
import com.omegafrog.My.piano.app.security.handler.CommonUserLoginSuccessHandler;
import com.omegafrog.My.piano.app.security.provider.CommonUserAuthenticationProvider;
import com.omegafrog.My.piano.app.security.service.CommonUserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
@Slf4j
public class SecurityConfig {
    @Autowired
    private SecurityUserRepository securityUserRepository;
    @Value("security.passwordEncoder.secret")
    private String secret;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new Pbkdf2PasswordEncoder(secret, 32, 256, Pbkdf2PasswordEncoder.SecretKeyFactoryAlgorithm.PBKDF2WithHmacSHA512);
    }

    @Autowired
    private ObjectMapper objectMapper;
    @Bean
    public CommonUserService commonUserService(){
        return new CommonUserService(passwordEncoder(), securityUserRepository);
    }


    @Bean
    public CommonUserAuthenticationProvider commonUserAuthenticationProvider(){
        return new CommonUserAuthenticationProvider(commonUserService(), passwordEncoder());
    }

    @Bean
    public SecurityFilterChain commonUserAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/user/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/user/register")
                .permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin().permitAll()
                .loginProcessingUrl("/user/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(new CommonUserLoginSuccessHandler(objectMapper))
                .failureHandler(new CommonUserLoginFailureHandler(objectMapper))
                .and()
                .csrf().disable();

        return http.build();
    }
}
