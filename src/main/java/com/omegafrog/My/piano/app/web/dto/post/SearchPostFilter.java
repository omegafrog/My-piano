package com.omegafrog.My.piano.app.web.dto.post;

import com.omegafrog.My.piano.app.web.domain.post.QPost;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

public record SearchPostFilter(Long id, Long authorId) {

    public BooleanExpression getExpression(){
        QPost post = QPost.post;
        BooleanExpression expression = Expressions.asBoolean(true).isTrue();
        if(id!=null) expression = expression.and(post.id.eq(id));
        if(authorId!=null) expression = expression.and(post.author.id.eq(authorId));
        return expression;
    }

}
