package com.omegafrog.My.piano.app.security.handler;

public interface LogoutBlacklistRepository {
    String save(String accessToken);
    boolean isPresent(String accessToken);
    void delete(String accessToken);
}
