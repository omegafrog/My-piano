package com.omegafrog.My.piano.app.security.reposiotry;

import com.omegafrog.My.piano.app.security.handler.LogoutBlacklistRepository;
import com.omegafrog.My.piano.app.security.jwt.TokenUtils;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class InMemoryLogoutBlacklistRepository implements LogoutBlacklistRepository {

    private final String jwtSecret;

    private static final Map<String, Boolean> storage = new ConcurrentHashMap<>();

    @Override
    public String save(String accessToken) {
        storage.put(accessToken, true);
        return accessToken;
    }

    @Override
    public boolean isPresent(String accessToken) {
        if(storage.containsKey(accessToken)){
            return storage.get(accessToken);
        }else{
            return false;
        }

    }

    @Override
    public void delete(String accessToken) {
        if(storage.containsKey(accessToken)){
            storage.remove(accessToken);
        }else {
            return;
        }
    }
}
