package com.omegafrog.My.piano.app.security.infrastructure;

import com.omegafrog.My.piano.app.security.entity.QSecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUser;
import com.omegafrog.My.piano.app.security.entity.SecurityUserRepository;
import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Repository
public class SecurityUserRepositoryImpl implements SecurityUserRepository {

    @Override
    public Long count() {
        return null;
    }

    private final JpaSecurityUserRepository jpaRepository;
    private final JPAQueryFactory factory;

    @Override
    public SecurityUser save(SecurityUser securityUser) {
        return jpaRepository.save(securityUser);
    }

    @Override
    public Optional<SecurityUser> findByUsername(String username) {
        return jpaRepository.findByUsername(username);
    }

    @Override
    public Optional<SecurityUser> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<SecurityUser> findAll(Pageable pageable, SearchUserFilter filter) {
        QSecurityUser securityUser = QSecurityUser.securityUser;
        BooleanExpression queryPredicate = filter.getQueryPredicate();
        return factory.selectFrom(securityUser)
                .where(queryPredicate)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
