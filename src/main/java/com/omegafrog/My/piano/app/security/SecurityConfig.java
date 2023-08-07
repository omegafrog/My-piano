package com.omegafrog.My.piano.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.filter.JwtTokenFilter;
import com.omegafrog.My.piano.app.security.handler.*;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.provider.CommonUserAuthenticationProvider;
import com.omegafrog.My.piano.app.security.reposiotry.InMemoryLogoutBlacklistRepository;
import com.omegafrog.My.piano.app.security.service.CommonUserService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@Slf4j
public class SecurityConfig {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private SecurityUserRepository securityUserRepository;
    @Value("${security.passwordEncoder.secret}")
    private String secret;

    @Value("${security.jwt.secret}")
    private String jwtSecret;


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
    public LogoutBlacklistRepository inMemoryLogoutBlackListRepository(){
        return new InMemoryLogoutBlacklistRepository(jwtSecret);
    }

    @Bean
    public JwtTokenFilter jwtTokenFilter(){
        return new JwtTokenFilter(objectMapper, securityUserRepository,
                refreshTokenRepository, inMemoryLogoutBlackListRepository(), jwtSecret);
    }
    @Bean
    public CommonUserLogoutHandler commonUserLogoutHandler(){
        return new CommonUserLogoutHandler(objectMapper, inMemoryLogoutBlackListRepository());
    }

    @Bean
    public SecurityFilterChain commonUserAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/user/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/user/register")
                .permitAll()
                .anyRequest().hasRole(Role.USER.authorityName)
                .and()
                .formLogin().permitAll()
                .loginProcessingUrl("/user/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(new CommonUserLoginSuccessHandler(objectMapper, refreshTokenRepository, jwtSecret))
                .failureHandler(new CommonUserLoginFailureHandler(objectMapper))
                .and()
                .logout()
                .logoutUrl("/user/logout").permitAll()
                .addLogoutHandler(commonUserLogoutHandler())
                .and()
                .addFilterBefore(jwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(new AuthenticationExceptionEntryPoint(objectMapper))
                .and()
                .csrf().disable()
                .cors().disable();
        return http.build();
    }
    @Bean
    public SecurityFilterChain postAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/community/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/community/{id:[0-9]+}")
                .permitAll()
                .anyRequest().hasRole(Role.USER.authorityName)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
                .cors().disable();
        return http.build();
    }

    @Bean
    public SecurityFilterChain lessonAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/lesson/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/lesson/{id:[0-9]+}")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/lesson")
                .permitAll()
                .anyRequest().hasRole(Role.USER.authorityName)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .csrf().disable()
                .cors().disable();
        return http.build();
    }
}
