package com.omegafrog.My.piano.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.filter.JwtTokenExceptionFilter;
import com.omegafrog.My.piano.app.security.filter.JwtTokenFilter;
import com.omegafrog.My.piano.app.security.handler.*;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.security.provider.AdminAuthenticationProvider;
import com.omegafrog.My.piano.app.security.provider.CommonUserAuthenticationProvider;
import com.omegafrog.My.piano.app.security.reposiotry.InMemoryLogoutBlacklistRepository;
import com.omegafrog.My.piano.app.security.service.AdminUserService;
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
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@EnableWebSecurity
@Configuration
@Slf4j
public class SecurityConfig {


    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Bean
    public TokenUtils tokenUtils(){
        return new TokenUtils();
    }

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
    public CommonUserService commonUserService() {
        return new CommonUserService(passwordEncoder(), securityUserRepository, refreshTokenRepository);
    }
    @Bean
    public AdminUserService adminUserService(){
        return new AdminUserService();
    }


    @Bean
    public CommonUserAuthenticationProvider commonUserAuthenticationProvider() {
        return new CommonUserAuthenticationProvider(commonUserService(), passwordEncoder());
    }

    @Bean
    public CommonUserAccessDeniedHandler commonUserAccessDeniedHandler() {
        return new CommonUserAccessDeniedHandler(objectMapper);
    }

    @Bean
    public LogoutBlacklistRepository inMemoryLogoutBlackListRepository() {
        return new InMemoryLogoutBlacklistRepository(jwtSecret);
    }

    @Bean
    public JwtTokenFilter jwtTokenFilter() {
        return new JwtTokenFilter(objectMapper, securityUserRepository, refreshTokenRepository);
    }

    @Bean
    public CommonUserLogoutHandler commonUserLogoutHandler() {
        return new CommonUserLogoutHandler(objectMapper, refreshTokenRepository);
    }

    @Bean
    public AuthenticationEntryPoint UnAuthorizedEntryPoint() {
        return new AuthenticationExceptionEntryPoint(objectMapper);
    }

    @Bean
    public AdminAuthenticationProvider adminAuthenticationProvider(){
        return new AdminAuthenticationProvider(adminUserService(), passwordEncoder());
    }

    @Bean
    public JwtTokenExceptionFilter jwtTokenExceptionFilter() {
        return new JwtTokenExceptionFilter();
    }

    @Bean
    public SecurityFilterChain adminAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/admin/**")
                .authenticationProvider(adminAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/admin/login","/admin/register", "/admin/logout")
                .permitAll()
                .anyRequest()
                .hasRole(Role.ADMIN.value)
                .and()
                /*
                슈퍼 관리자 엔드포인트 권한 등록 필요
                .authorizeHttpRequests()
                .requestMatchers()
                */
                .formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(new AdminLoginSuccessHandler(objectMapper, refreshTokenRepository, tokenUtils()))
                .failureHandler(new CommonUserLoginFailureHandler(objectMapper))
                .loginProcessingUrl("/admin/login")
                .and()
                .logout()
                .logoutUrl("/admin/logout")
                .addLogoutHandler(commonUserLogoutHandler())
                .and()
                .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(UnAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public SecurityFilterChain oauth2Authentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/oauth2/**")
                .authorizeHttpRequests()
                .requestMatchers("/oauth2/**")
                .permitAll()
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }


    @Bean
    public SecurityFilterChain commonUserAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/user/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/user/register", "/user/profile/register")
                .permitAll()
                .requestMatchers("/user/login/**", "/user/logout/**")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .formLogin().permitAll()
                .loginProcessingUrl("/user/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(new CommonUserLoginSuccessHandler(objectMapper, refreshTokenRepository, tokenUtils()))
                .failureHandler(new CommonUserLoginFailureHandler(objectMapper))
                .and()
                .logout()
                .logoutUrl("/user/logout").permitAll()
                .addLogoutHandler(commonUserLogoutHandler())
                .and()
                .addFilterBefore(jwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), JwtTokenFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(UnAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public SecurityFilterChain postAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/community/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/community/post")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/community/video-post")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/community/post/{id:[0-9]+}")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/community/video-post/{id:[0-9]+}")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(UnAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .addFilterBefore(jwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), JwtTokenFilter.class)
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
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
                .anyRequest().hasRole(Role.CREATOR.value)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), JwtTokenFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(UnAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public SecurityFilterChain OrderAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/order/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/order")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/order/{id:[0-9]+}")
                .permitAll()
                .anyRequest().hasRole(Role.USER.value)
                .and()
                .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), JwtTokenFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(UnAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public SecurityFilterChain sheetPostAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/sheet/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/sheet/{id:[0-9]+}")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/sheet")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), JwtTokenFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(UnAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public  SecurityFilterChain cartAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/cart/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/cart")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), JwtTokenFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(UnAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());

        return http.build();
    }

    @Bean
    SecurityFilterChain h2console(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/h2-console/**")
                .authorizeHttpRequests()
                .anyRequest().permitAll()
                .and()
                .csrf().disable()
                .cors().disable();
        return http.build();
    }

    @Bean
    public SecurityFilterChain defaultConfig(HttpSecurity http) throws Exception {
        http.
                securityMatcher("/**")
                .authorizeHttpRequests()
                .anyRequest().permitAll()
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource())
                .and()
                .headers()
                .frameOptions()
                .sameOrigin();
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedHeader("*");
        configuration.addAllowedMethod("*");
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
