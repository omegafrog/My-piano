package com.omegafrog.My.piano.app.web.dto.lesson;

import com.omegafrog.My.piano.app.web.domain.lesson.QLesson;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.time.LocalDate;

public record SearchLessonFilter(Long id, String title, LocalDate startDate, LocalDate endDate) {
    public BooleanExpression getExpression() {
        QLesson lesson = QLesson.lesson;
        BooleanExpression expression = Expressions.asBoolean(Boolean.TRUE).isTrue();
        if(id!=null) expression = expression.and(lesson.id.eq(id));
        if(title!=null) expression = expression.and(lesson.title.eq(title));
        if(startDate!=null && endDate!=null) expression = expression.and(lesson.createdAt.between(
                startDate.atStartOfDay(), endDate.atTime(23, 59)
        ));
        return expression;
    }
}
