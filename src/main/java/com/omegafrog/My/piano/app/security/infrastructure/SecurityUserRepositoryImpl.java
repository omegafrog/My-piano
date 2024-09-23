package com.omegafrog.My.piano.app.security.infrastructure;

import com.omegafrog.My.piano.app.web.domain.user.*;
import com.omegafrog.My.piano.app.web.dto.admin.SearchUserFilter;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Repository
public class SecurityUserRepositoryImpl implements SecurityUserRepository {


    private final JpaSecurityUserRepository jpaRepository;
    private final JPAQueryFactory factory;

    @Override
    public SecurityUser save(SecurityUser securityUser) {
        if (securityUser.getUser().getSecurityUser() == null) securityUser.getUser().setSecurityUser(securityUser);
        return jpaRepository.save(securityUser);
    }

    @Override
    public Long count() {
        return jpaRepository.count();
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
    public Page<SecurityUser> findAllByFilter(Pageable pageable, SearchUserFilter filter) {
        QSecurityUser securityUser = QSecurityUser.securityUser;
        BooleanExpression queryPredicate = filter.getQueryPredicate();

        JPAQuery<SecurityUser> query = factory.selectFrom(securityUser)
                .where(queryPredicate)
                .join(QUser.user, securityUser.user);

        List<SecurityUser> fetched = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        Long size = (long) query.fetch().size();

        return PageableExecutionUtils.getPage(fetched, pageable, () -> size);
    }


    @Override
    public Page<SecurityUser> findAllByUserId(List<Long> userId, Pageable pageable) {
        QSecurityUser securityUser = QSecurityUser.securityUser;
        BooleanExpression queryPredicate = Expressions.allOf(securityUser.user.id.in(userId));
        List<SecurityUser> fetched = factory.selectFrom(securityUser)
                .where(queryPredicate)
                .join(QUser.user, securityUser.user).fetchJoin()
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long size = (long) factory.selectFrom(securityUser)
                .where(queryPredicate)
                .fetch().size();
        return PageableExecutionUtils.getPage(fetched, pageable, () -> size);
    }

    @Override
    public Optional<SecurityUser> findByEmail(String email) {
        QUser user = QUser.user;
        User fetchedUser = factory.selectFrom(user)
                .where(user.email.eq(email))
                .fetchOne();
        if (fetchedUser == null) return Optional.empty();
        return Optional.ofNullable(fetchedUser.getSecurityUser());
    }
}
