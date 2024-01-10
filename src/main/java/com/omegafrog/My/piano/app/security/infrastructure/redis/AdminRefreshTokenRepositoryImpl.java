package com.omegafrog.My.piano.app.security.infrastructure.redis;

import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import jakarta.persistence.EntityExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
public class AdminRefreshTokenRepositoryImpl implements RefreshTokenRepository {

    @Autowired
    @Qualifier("AdminRedisTemplate")
    private RedisTemplate redisTemplate;

    @Override
    public void deleteAll() {

    }
    @Override
    public RefreshToken save(RefreshToken token) {
        if(token.getId()!=null)
            throw new EntityExistsException("RefreshToken is already exist.");

        String id = String.valueOf(UUID.randomUUID());
        SetOperations ops = redisTemplate.opsForSet();
        ops.add("refresh", id);
        Map<String, Object> hashedToken = new HashMap<>();
        hashedToken.put("userId", token.getUserId().toString());
        hashedToken.put("refreshToken", token.getRefreshToken());
        hashedToken.put("role", token.getRole().value);
        hashedToken.put("id", id);
        hashedToken.put("expiration", token.getExpiration().toString());
        hashedToken.put("_class", "com.omegafrog.My.piano.app.security.jwt.RefreshToken");
        redisTemplate.opsForHash().putAll("refresh:"+id, hashedToken);
        ops.add("refresh:userId:" + token.getUserId(), id);
        ops.add("refresh:" + id + ":idx", "refresh:userId:" + token.getUserId());
        redisTemplate.expire("refresh:" + id, token.getExpiration(), TimeUnit.MILLISECONDS);
        log.info("ttl setted.");
        return RefreshToken.builder()
                .id(id)
                .userId(token.getUserId())
                .role(token.getRole())
                .expiration(token.getExpiration())
                .build();

    }

    @Override
    public List<RefreshToken> findAll() {
        return new ArrayList<>();
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        SetOperations<String, Object> ops = redisTemplate.opsForSet();
        Set<Object> members = ops.members("refresh:userId:"+userId);
        if (members == null || members.isEmpty()) return Optional.empty();

        String member = (String) members.toArray()[0];
        Map entries = redisTemplate.opsForHash().entries("refresh:" + member);
        RefreshToken founded = RefreshToken.builder()
                .refreshToken((String) entries.get("refreshToken"))
                .userId(Long.valueOf((String) entries.get("userId")))
                .expiration(Long.valueOf((String)entries.get("expiration")))
                .build();
        return Optional.of(founded);

    }

    @Override
    public void deleteByUserId(Long userId) {
        SetOperations<String, Object> ops = redisTemplate.opsForSet();
        Set<Object> members = ops.members("refresh:userId:"+userId);
        String refreshKeyId = (String) members.toArray()[0];
        redisTemplate.delete("refresh:" + refreshKeyId + ":idx");
        redisTemplate.delete("refresh:userId:" + userId);
        redisTemplate.delete("refresh:" + refreshKeyId);
        ops.remove("refresh",refreshKeyId );
    }

    @Override
    public void deleteById(Long id) {
    }
}
