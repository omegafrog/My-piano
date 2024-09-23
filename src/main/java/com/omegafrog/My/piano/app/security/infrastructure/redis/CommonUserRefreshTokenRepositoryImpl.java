package com.omegafrog.My.piano.app.security.infrastructure.redis;

import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CommonUserRefreshTokenRepositoryImpl implements RefreshTokenRepository {

    @Autowired
    private RedisRefreshTokenRepository jpaRepository;
    @Autowired
    @Qualifier("CommonUserRedisTemplate")
    private RedisTemplate redisTemplate;

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        String key = "refresh:" + token.getRole().value + ":" + token.getUserId();
        redisTemplate.opsForHash().putAll("refresh:" + token.getRole().value + ":" + token.getUserId(),
                Map.of("id", token.getUserId().toString(),
                        "refreshToken", token.getPayload(),
                        "userId", token.getUserId().toString(),
                        "expiration", token.getExpiration().toString(),
                        "role", token.getRole().value,
                        "createdAt", token.getCreatedAt().toString()));
        redisTemplate.opsForSet().add("refresh:" + token.getRole().value, token.getUserId().toString());
        redisTemplate.expireAt(key, Instant.now().plusSeconds(token.getExpiration()));
        token.setId(token.getRole() + ":" + token.getUserId());
        return token;
    }

    @Override
    public List<RefreshToken> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public void deleteByUserIdAndRole(Long userId, Role role) {
        redisTemplate.opsForSet().remove("refresh:" + role.value, userId.toString());
        redisTemplate.opsForHash().delete("refresh:" + role.value + ":" + userId,
                "id", "expiration", "refreshToken", "userId", "createdAt", "role");
    }

    @Override
    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Optional<RefreshToken> findByRoleAndUserId(Long userId, Role role) {
        String roleKey = role.value;
        HashOperations<String, String, Object> hashOperations = redisTemplate.opsForHash();
        Map<String, Object> refreshToken = hashOperations.entries("refresh:" + roleKey + ":" + userId);
        if (refreshToken.isEmpty()) return Optional.empty();
        return Optional.of(new RefreshToken((String) refreshToken.get("id"), (String) refreshToken.get("refreshToken"),
                Long.parseLong((String) refreshToken.get("userId")), Long.parseLong((String) refreshToken.get("expiration")),
                Role.valueOf((String) refreshToken.get("role")), LocalDateTime.parse((String) refreshToken.get("createdAt"))));
    }

    @Override
    public Page<RefreshToken> findAllByRole(Role[] role, Pageable pageable) {
        List<RefreshToken> list = new ArrayList<>();
        long count = 0;
        for (Role r : role) {
            Set members = redisTemplate.opsForSet().members("refresh:" + r.value);
            count += members.size();
            for (Object member : members) {
                String item = String.valueOf(member);
                Map<String, Object> entries = redisTemplate.opsForHash().entries("refresh:" + r.value + ":" + item);
                list.add(new RefreshToken((String) entries.get("id"), (String) entries.get("refreshToken"),
                        Long.parseLong((String) entries.get("userId")), Long.parseLong((String) entries.get("expiration")),
                        Role.valueOf((String) entries.get("role")), LocalDateTime.parse((String) entries.get("createdAt"))));
                if (list.size() == pageable.getPageSize()) break;
            }
            if (list.size() == pageable.getPageSize()) break;
        }
        long finalCount = count;
        return PageableExecutionUtils.getPage(list, pageable, () -> finalCount);
    }

    private Long countKeys(String pattern) {
        Cursor<byte[]> cursor = redisTemplate.getConnectionFactory().getConnection().scan(ScanOptions.scanOptions().match(pattern).build());
        return cursor.stream().count();
    }

    @Override
    public Long countByRole(Role role) {
        return redisTemplate.opsForSet().size("refresh:" + role.value);

    }

}
