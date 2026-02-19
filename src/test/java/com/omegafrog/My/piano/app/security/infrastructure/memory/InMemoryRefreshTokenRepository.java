package com.omegafrog.My.piano.app.security.infrastructure.memory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.omegafrog.My.piano.app.TestResettable;
import com.omegafrog.My.piano.app.security.jwt.RefreshToken;
import com.omegafrog.My.piano.app.security.jwt.RefreshTokenRepository;
import com.omegafrog.My.piano.app.web.domain.user.authorities.Role;

@Repository
@Profile("test")
public class InMemoryRefreshTokenRepository implements RefreshTokenRepository, TestResettable {

    private final Map<String, RefreshToken> byId = new ConcurrentHashMap<>();

    private static String idOf(Role role, Long userId) {
        return role.value + ":" + userId;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        String id = idOf(token.getRole(), token.getUserId());
        token.setId(id);
        byId.put(id, token);
        return token;
    }

    @Override
    public List<RefreshToken> findAll() {
        return new ArrayList<>(byId.values());
    }

    @Override
    public Optional<RefreshToken> findByRoleAndUserId(Long userId, Role role) {
        return Optional.ofNullable(byId.get(idOf(role, userId)));
    }

    @Override
    public void deleteByUserIdAndRole(Long userId, Role role) {
        byId.remove(idOf(role, userId));
    }

    @Override
    public void deleteById(String id) {
        byId.remove(id);
    }

    @Override
    public void deleteAll() {
        byId.clear();
    }

    @Override
    public Page<RefreshToken> findAllByRole(Role[] role, Pageable pageable) {
        List<RefreshToken> items = byId.values().stream()
                .filter(token -> {
                    for (Role r : role) {
                        if (token.getRole() == r) {
                            return true;
                        }
                    }
                    return false;
                })
                .sorted(Comparator.comparing(RefreshToken::getCreatedAt))
                .toList();

        int start = Math.toIntExact(pageable.getOffset());
        int end = Math.min(items.size(), start + pageable.getPageSize());
        if (start >= items.size()) {
            return Page.empty(pageable);
        }
        return new PageImpl<>(items.subList(start, end), pageable, items.size());
    }

    @Override
    public Long countByRole(Role role) {
        return byId.values().stream().filter(token -> token.getRole() == role).count();
    }

    @Override
    public void reset() {
        deleteAll();
    }
}
