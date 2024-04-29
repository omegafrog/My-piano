package com.omegafrog.My.piano.app.web.dto.sheetPost;

import com.omegafrog.My.piano.app.web.domain.sheet.QSheetPost;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record SearchSheetPostFilter(Long id, String title, String username, Long sheetId,
                                    com.omegafrog.My.piano.app.web.enums.Instrument instrument,
                                    com.omegafrog.My.piano.app.web.enums.Difficulty difficulty, LocalDate dateStart,
                                    LocalDate dateEnd) {
    public BooleanExpression getExpressions(){
        QSheetPost sheetPost = QSheetPost.sheetPost;
        BooleanExpression expression = Expressions.asBoolean(Boolean.TRUE).isTrue();
        if(id!=null) expression = expression.and(sheetPost.id.eq(id));
        if(title !=null) expression = expression.and(sheetPost.title.contains(title));
        if(username!=null) expression = expression.and(sheetPost.author.securityUser.username.eq(username));
        if(instrument!=null)
            expression = expression.and(sheetPost.sheet.instrument.eq(instrument));
        if (difficulty!=null)
            expression = expression.and(sheetPost.sheet.difficulty.eq(difficulty));
        if(sheetId!=null) expression = expression.and(sheetPost.sheet.id.eq(sheetId));
        if(dateStart!=null && dateEnd!=null) expression = expression.and(sheetPost.createdAt.between(
                dateStart.atStartOfDay(), dateEnd.atStartOfDay()
        ));
        return expression;
    }

}
