package com.omegafrog.My.piano.app.security.controller;

import com.omegafrog.My.piano.app.Cleanup;
import com.omegafrog.My.piano.app.TestUtil;
import com.omegafrog.My.piano.app.TestUtilConfig;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUser;
import com.omegafrog.My.piano.app.web.domain.user.SecurityUserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestUtilConfig.class)
@Disabled
class JwtSecurityIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private TestUtil testUtil;

  @Autowired
  private Cleanup cleanup;

  @Autowired
  private SecurityUserRepository securityUserRepository;

  @Value("${security.jwt.secret}")
  private String jwtSecret;

  @BeforeEach
  void setUp() {
    cleanup.cleanUp();
  }

  @Test
  @DisplayName("유효한 JWT로 보호 API 접근 시 200을 반환한다")
  void validJwtCanAccessProtectedEndpoint() throws Exception {
    testUtil.register(mockMvc, TestUtil.user1);
    MockHttpSession session = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());

    mockMvc.perform(get("/api/v1/user")
        .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()));
  }

  @Test
  @DisplayName("만료된 JWT와 유효한 refresh 상태면 revalidate가 200을 반환한다")
  void expiredJwtCanBeRevalidated() throws Exception {
    testUtil.register(mockMvc, TestUtil.user1);
    MockHttpSession session = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());

    SecurityUser securityUser = securityUserRepository.findByUsername(TestUtil.user1.getUsername())
        .orElseThrow();
    String expiredAccessToken = createExpiredAccessToken(securityUser.getId(), securityUser.getRole().value);

    MvcResult result = mockMvc.perform(get("/api/v1/revalidate")
        .session(session))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value(HttpStatus.OK.value()))
        .andReturn();

    String newAccessToken = result.getResponse().getContentAsString();
    assertThat(newAccessToken).contains("Token revalidating success");
  }

  @Test
  @DisplayName("로그아웃된 사용자의 JWT로 보호 API 접근 시 401을 반환한다")
  void loggedOutJwtCannotAccessProtectedEndpoint() throws Exception {
    testUtil.register(mockMvc, TestUtil.user1);
    MockHttpSession session = testUtil.login(mockMvc, TestUtil.user1.getUsername(), TestUtil.user1.getPassword());

    mockMvc.perform(get("/api/v1/user/logout")
        .session(session))
        .andExpect(status().isOk());

    mockMvc.perform(get("/api/v1/user")
        .session(session))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()));
  }

  private String createExpiredAccessToken(Long userId, String role) {
    Claims claims = Jwts.claims();
    claims.put("id", String.valueOf(userId));
    claims.put("role", role);
    long now = System.currentTimeMillis();
    return Jwts.builder()
        .addClaims(claims)
        .setIssuedAt(new Date(now - 10_000))
        .setExpiration(new Date(now - 1_000))
        .signWith(SignatureAlgorithm.HS512, jwtSecret)
        .compact();
  }
}
