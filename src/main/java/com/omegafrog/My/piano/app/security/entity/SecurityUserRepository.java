package com.omegafrog.My.piano.app.security.entity;

import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SecurityUserRepository {
    SecurityUser save(SecurityUser securityUser);
    Optional<SecurityUser> findByUsername(String username);

    Optional<SecurityUser> findById(Long id);
    void deleteById(Long id);

    List<SecurityUser> findAll(Pageable pageable, SearchUserFilter filter);

    Long count();
}
