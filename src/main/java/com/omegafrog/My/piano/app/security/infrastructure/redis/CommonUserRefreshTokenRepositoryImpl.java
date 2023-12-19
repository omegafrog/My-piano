package com.omegafrog.My.piano.app.security.infrastructure.redis;

import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Repository
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
        return jpaRepository.save(token);
    }

    @Override
    public List<RefreshToken> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId);
//        SetOperations<String, Object> ops = redisTemplate.opsForSet();
//        Set<Object> members = ops.members("refresh:userId:"+userId);
//        if (members == null || members.isEmpty()) return Optional.empty();
//
//        String member = (String) members.toArray()[0];
//        Map entries = redisTemplate.opsForHash().entries("refresh:" + member);
//        RefreshToken founded = RefreshToken.builder()
//                .refreshToken((String) entries.get("refreshToken"))
//                .userId(Long.valueOf((String) entries.get("userId")))
//                .expiration(Long.valueOf((String)entries.get("expiration")))
//                .build();
//        return Optional.of(founded);

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
        jpaRepository.deleteById(id);
    }
}
