package com.omegafrog.My.piano.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GooglePublicKeysManager;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.filter.JwtTokenFilter;
import com.omegafrog.My.piano.app.security.handler.*;
import com.omegafrog.My.piano.app.security.infrastructure.redis.CommonUserRefreshTokenRepositoryImpl;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.security.provider.AdminAuthenticationProvider;
import com.omegafrog.My.piano.app.security.provider.CommonUserAuthenticationProvider;
import com.omegafrog.My.piano.app.utils.AuthenticationUtil;
import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.service.admin.AdminUserService;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import software.amazon.awssdk.services.s3.S3Client;

@EnableWebSecurity
@Configuration
@Slf4j
public class SecurityConfig {


    @Autowired
    private S3Client s3Client;

    @Bean
    public RefreshTokenRepository refreshTokenRepository() {
        return new CommonUserRefreshTokenRepositoryImpl();
    }

    @Bean
    public TokenUtils tokenUtils() {
        return new TokenUtils();
    }

    @Bean
    public JwtTokenFilter jwtFilter() {
        return new JwtTokenFilter(tokenUtils(), securityUserRepository, refreshTokenRepository());
    }

    @Autowired
    private SecurityUserRepository securityUserRepository;
    @Autowired
    private GooglePublicKeysManager googlePublicKeysManager;

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
        return new CommonUserService(passwordEncoder(),
                securityUserRepository,
                refreshTokenRepository(),
                googlePublicKeysManager,
                authenticationUtil,
                s3Client);
    }

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LessonRepository lessonRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private SheetPostRepository sheetPostRepository;
    @Autowired
    private MapperUtil mapperUtil;
    @Autowired
    private AuthenticationUtil authenticationUtil;

    @Bean
    public AdminUserService adminUserService() {
        return new AdminUserService(
                passwordEncoder(),
                userRepository,
                refreshTokenRepository(),
                securityUserRepository,
                postRepository,
                mapperUtil,
                sheetPostRepository,
                lessonRepository,
                authenticationUtil
        );
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
    public CommonUserLogoutHandler commonUserLogoutHandler() {
        return new CommonUserLogoutHandler(objectMapper, refreshTokenRepository());
    }

    @Bean
    public AuthenticationEntryPoint unAuthorizedEntryPoint() {
        return new AuthenticationExceptionEntryPoint(objectMapper);
    }

    @Bean
    public AdminAuthenticationProvider adminAuthenticationProvider() {
        return new AdminAuthenticationProvider(adminUserService(), passwordEncoder());
    }

    @Bean
    public SecurityFilterChain commentAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/**/comments/**")
                .authenticationProvider(commonUserAuthenticationProvider()).authorizeHttpRequests(
                        (authorizeHttpRequest) ->
                                authorizeHttpRequest
                                        .requestMatchers(HttpMethod.GET, "/api/v1/**/comments")
                                        .permitAll()
                                        .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                )
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling((exceptionadvisor) ->
                        exceptionadvisor
                                .authenticationEntryPoint(unAuthorizedEntryPoint())
                                .accessDeniedHandler(commonUserAccessDeniedHandler())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors((conf) -> conf.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    @Bean
    public SecurityFilterChain adminAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/admin/**")
                .authenticationProvider(adminAuthenticationProvider()).authorizeHttpRequests(
                        (authorizeHttpRequest) ->
                                authorizeHttpRequest
                                        .requestMatchers("/api/v1/admin/login", "/api/v1/admin/register", "/api/v1/admin/logout")
                                        .permitAll()
                                        .anyRequest()
                                        .hasAnyRole(Role.ADMIN.value, Role.SUPER_ADMIN.value)
                )
                .formLogin((formLogin) ->
                        formLogin.usernameParameter("username")
                                .passwordParameter("password")
                                .successHandler(new AdminLoginSuccessHandler(objectMapper, refreshTokenRepository(), tokenUtils()))
                                .failureHandler(new CommonUserLoginFailureHandler(objectMapper))
                                .loginProcessingUrl("/api/v1/admin/login"))
                .logout((logout) ->
                        logout.logoutUrl("/api/v1/admin/logout")
                                .addLogoutHandler(commonUserLogoutHandler()))
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling((exceptionadvisor) ->
                        exceptionadvisor
                                .authenticationEntryPoint(unAuthorizedEntryPoint())
                                .accessDeniedHandler(commonUserAccessDeniedHandler())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors((conf) -> conf.configurationSource(corsConfigurationSource()));

        return http.build();
    }

    @Bean
    public SecurityFilterChain oauth2Authentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/oauth2/**")
                .authorizeHttpRequests()
                .requestMatchers("/api/v1/oauth2/**")
                .permitAll()
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public SecurityFilterChain cashApiAuthentication(HttpSecurity http) throws Exception {
        http.securityMatcher("/api/v1/cash/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/api/v1/cash/webhook")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }


    @Bean
    public SecurityFilterChain commonUserAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/user/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/api/v1/user/register", "/api/v1/user/profile/register")
                .permitAll()
                .requestMatchers("/api/v1/user/login/**", "/api/v1/user/logout/**")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .formLogin().permitAll()
                .loginProcessingUrl("/api/v1/user/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(new CommonUserLoginSuccessHandler(objectMapper, refreshTokenRepository(), tokenUtils()))
                .failureHandler(new CommonUserLoginFailureHandler(objectMapper))
                .and()
                .logout()
                .logoutUrl("/api/v1/user/logout").permitAll()
                .logoutSuccessHandler(logoutHandler())
                .addLogoutHandler(commonUserLogoutHandler())
                .and()
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());

        return http.build();
    }

    private LogoutSuccessHandler logoutHandler() {
        return new CommonUserLogoutSuccessHandler(objectMapper);
    }

    @Bean
    public SecurityFilterChain postAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/community/posts/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/api/v1/community/posts",
                        "/api/v1/community/video-post",
                        "/api/v1/community/posts/{id:[0-9]+}",
                        "/api/v1/community/video-post/{id:[0-9]+}")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public SecurityFilterChain lessonAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/lessons/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET,
                        "/api/v1/lessons",
                        "/api/v1/lessons/{id:[0-9]+}")
                .permitAll()
                .requestMatchers("/api/v1/lessons/{id:[0-9]+}/scrap", "/api/v1/lessons/{id:[0-9]+}/like")
                .hasAnyRole(Role.CREATOR.value, Role.USER.value)
                .anyRequest().hasRole(Role.CREATOR.value)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public SecurityFilterChain CartAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/cart/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests(
                        authorizationManagerRequestMatcherRegistry ->
                                authorizationManagerRequestMatcherRegistry
                                        .requestMatchers("/api/v1/cart/{mainResource:sheet|lessons}")
                                        .hasAnyRole(Role.USER.value, Role.CREATOR.value)
                                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .sessionManagement((sessionManagement) ->
                        sessionManagement
                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .exceptionHandling((exceptionadvisor) ->
                        exceptionadvisor
                                .authenticationEntryPoint(unAuthorizedEntryPoint())
                                .accessDeniedHandler(commonUserAccessDeniedHandler())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors((conf) -> conf.configurationSource(corsConfigurationSource()));
        return http.build();
    }

    @Bean
    public SecurityFilterChain OrderAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/order/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/api/v1/order",
                        "/api/v1/order/{id:[0-9]+}")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public SecurityFilterChain sheetPostAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/sheet-post/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/api/v1/sheet-post/{id:[0-9]+}")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/sheet-post")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        return http.build();
    }

    @Bean
    public SecurityFilterChain cartAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/cart/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/api/v1/cart")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
                .accessDeniedHandler(commonUserAccessDeniedHandler())
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());

        return http.build();
    }

    @Bean
    SecurityFilterChain ticketAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/v1/tickets")
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.PUT, "/api/v1/tickets")
                .hasRole(Role.USER.value)
                .anyRequest().permitAll()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(jwtFilter(), AuthorizationFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
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
                securityMatcher("/api/v1/**")
                .authorizeHttpRequests(
                        authorizationManagerRequestMatcherRegistry ->
                                authorizationManagerRequestMatcherRegistry
                                        .anyRequest().permitAll()

                )
                .csrf(
                        httpSecurityCsrfConfigurer ->
                                httpSecurityCsrfConfigurer.disable()
                )
                .cors(
                        httpSecurityCorsConfigurer ->
                                httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource())
                )
                .headers(
                        httpSecurityHeadersConfigurer ->
                                httpSecurityHeadersConfigurer
                                        .frameOptions(
                                                HeadersConfigurer.FrameOptionsConfig::sameOrigin
                                        )
                );
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
