package com.omegafrog.My.piano.app.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.security.entity.authorities.Role;
import com.omegafrog.My.piano.app.security.filter.JwtTokenExceptionFilter;
import com.omegafrog.My.piano.app.security.filter.CommonUserJwtTokenFilter;
import com.omegafrog.My.piano.app.security.handler.*;
import com.omegafrog.My.piano.app.security.infrastructure.redis.CommonUserRefreshTokenRepositoryImpl;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import com.omegafrog.My.piano.app.security.provider.AdminAuthenticationProvider;
import com.omegafrog.My.piano.app.security.provider.CommonUserAuthenticationProvider;
import com.omegafrog.My.piano.app.security.reposiotry.InMemoryLogoutBlacklistRepository;
import com.omegafrog.My.piano.app.web.domain.lesson.LessonRepository;
import com.omegafrog.My.piano.app.web.service.admin.AdminUserService;
import com.omegafrog.My.piano.app.web.service.admin.CommonUserService;

import com.omegafrog.My.piano.app.utils.MapperUtil;
import com.omegafrog.My.piano.app.web.domain.admin.AdminRepository;
import com.omegafrog.My.piano.app.web.domain.post.PostRepository;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
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
    private AdminRepository adminRepository;

    @Bean
    public RefreshTokenRepository refreshTokenRepository(){
        return new CommonUserRefreshTokenRepositoryImpl();
    }
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
        return new CommonUserService(passwordEncoder(), securityUserRepository, refreshTokenRepository());
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

    @Bean
    public AdminUserService adminUserService(){
        return new AdminUserService(
                passwordEncoder(),
                userRepository,
                refreshTokenRepository(),
                securityUserRepository,
                postRepository,
                mapperUtil,
                sheetPostRepository,
                lessonRepository
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
    public LogoutBlacklistRepository inMemoryLogoutBlackListRepository() {
        return new InMemoryLogoutBlacklistRepository(jwtSecret);
    }

    @Bean
    public CommonUserJwtTokenFilter commonUserJwtTokenFilter() {
        return new CommonUserJwtTokenFilter( securityUserRepository, refreshTokenRepository() );
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
                .requestMatchers("/admin/login", "/admin/register", "/admin/logout")
                .permitAll()
                .anyRequest()
                .hasAnyRole(Role.ADMIN.value, Role.SUPER_ADMIN.value)
                .and()
                .formLogin()
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler(new AdminLoginSuccessHandler(objectMapper, refreshTokenRepository(), tokenUtils()))
                .failureHandler(new CommonUserLoginFailureHandler(objectMapper))
                .loginProcessingUrl("/admin/login")
                .and()
                .logout()
                .logoutUrl("/admin/logout")
                .addLogoutHandler(commonUserLogoutHandler())
                .and()
                .addFilterBefore(commonUserJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), CommonUserJwtTokenFilter.class)
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
    public SecurityFilterChain cashApiAuthentication(HttpSecurity http) throws Exception {
        http.securityMatcher("/cash/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers("/cash/webhook")
                .permitAll()
                .anyRequest().hasRole(Role.USER.value)
                .and()
                .addFilterBefore(commonUserJwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), CommonUserJwtTokenFilter.class)
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
                .successHandler(new CommonUserLoginSuccessHandler(objectMapper, refreshTokenRepository(), tokenUtils()))
                .failureHandler(new CommonUserLoginFailureHandler(objectMapper))
                .and()
                .logout()
                .logoutUrl("/user/logout").permitAll()
                .addLogoutHandler(commonUserLogoutHandler())
                .and()
                .addFilterBefore(commonUserJwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), CommonUserJwtTokenFilter.class)
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
    public SecurityFilterChain postAuthentication(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/community/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/community/posts")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/community/video-post")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/community/posts/{id:[0-9]+}")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/community/video-post/{id:[0-9]+}")
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
                .addFilterBefore(commonUserJwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), CommonUserJwtTokenFilter.class)
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
                .addFilterBefore(commonUserJwtTokenFilter(),
                        UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), CommonUserJwtTokenFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
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
                .anyRequest().hasAnyRole(Role.USER.value,Role.CREATOR.value)
                .and()
                .addFilterBefore(commonUserJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), CommonUserJwtTokenFilter.class)
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
                .securityMatcher("/sheet-post/**")
                .authenticationProvider(commonUserAuthenticationProvider())
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.GET, "/sheet-post/{id:[0-9]+}",
                        "/sheet-post/{id:[0-9]+}/comments")
                .permitAll()
                .requestMatchers(HttpMethod.GET, "/sheet-post")
                .permitAll()
                .anyRequest().hasAnyRole(Role.USER.value, Role.CREATOR.value)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(commonUserJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), CommonUserJwtTokenFilter.class)
                .exceptionHandling()
                .authenticationEntryPoint(unAuthorizedEntryPoint())
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
                .addFilterBefore(commonUserJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), CommonUserJwtTokenFilter.class)
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
                .securityMatcher("/tickets")
                .authorizeHttpRequests()
                .requestMatchers(HttpMethod.PUT, "/tickets")
                .hasRole(Role.USER.value)
                .anyRequest().permitAll()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .addFilterBefore(commonUserJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtTokenExceptionFilter(), CommonUserJwtTokenFilter.class)
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
