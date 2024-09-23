package com.omegafrog.My.piano.app.web.dto.admin;

import com.omegafrog.My.piano.app.web.domain.user.QSecurityUser;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.time.LocalDateTime;

public record SearchUserFilter(Long id, String email, String username,
                               com.omegafrog.My.piano.app.web.vo.user.LoginMethod loginMethod, String dateStart,
                               String dateEnd, Boolean locked) {

    public BooleanExpression getQueryPredicate() {
        QSecurityUser securityUser = QSecurityUser.securityUser;
        BooleanExpression predicate = Expressions.asBoolean(true).isTrue();
        if (id() != null) predicate = predicate.and(securityUser.id.eq(id()));
        if (username() != null) predicate = predicate.and(securityUser.username.eq(username()));
        if (!email().isEmpty()) predicate = predicate.and(securityUser.user.email.eq(email()));
        if (loginMethod() != null) predicate = predicate.and(securityUser.user.loginMethod.eq(loginMethod()));
        if (locked() != null) predicate = predicate.and(securityUser.locked.eq(locked()));
        if (!dateStart().isEmpty() && !dateEnd().isEmpty())
            predicate = predicate.and(securityUser.createdAt
                    .between(LocalDateTime.parse(dateStart()),
                            LocalDateTime.parse(dateEnd())));
        return predicate;
    }
}
