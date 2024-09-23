package com.omegafrog.My.piano.app.web.dto.ticket;

import com.omegafrog.My.piano.app.web.domain.ticket.QTicket;
import com.omegafrog.My.piano.app.web.enums.TicketType;
import com.omegafrog.My.piano.app.web.domain.ticket.TicketStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record SearchTicketFilter(Long id, TicketType type,
                                 TicketStatus status, LocalDate startDate,
                                 LocalDate endDate) {

    public BooleanExpression getQueryDslExpression() {
        QTicket ticket = QTicket.ticket;
        BooleanExpression expression = Expressions.asBoolean(true).isTrue();
        if (id != null) expression = expression.and(ticket.id.eq(id));
        if (status != null) expression = expression.and(ticket.status.eq(status));
        if (type != null) expression = expression.and(ticket.type.eq(type));
        if (endDate!=null && startDate!= null && !endDate.equals("") && !startDate.equals(""))
            expression = expression.and(ticket.createdAt.between(
                    LocalDateTime.of(startDate, LocalTime.of(0,0,0,0)),
                    LocalDateTime.of(endDate,LocalTime.of(0,0,0,0))));
        return expression;
    }
}

