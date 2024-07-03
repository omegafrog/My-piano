package com.omegafrog.My.piano.app.web.infra.ticket;

import com.omegafrog.My.piano.app.web.domain.ticket.QTicket;
import com.omegafrog.My.piano.app.web.domain.ticket.Ticket;
import com.omegafrog.My.piano.app.web.domain.ticket.TicketRepository;
import com.omegafrog.My.piano.app.web.dto.ticket.SearchTicketFilter;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class TicketRepositoryImpl implements TicketRepository {

    private final JpaRepository<Ticket, Long> jpaRepository;
    private final JPAQueryFactory factory;
    @Override
    public Ticket save(Ticket ticket) {
        return jpaRepository.save(ticket);
    }

    @Override
    public Optional<Ticket> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public Long count() {
        return jpaRepository.count();
    }

    @Override
    public Page<Ticket> findAll(SearchTicketFilter filter, Pageable pageable) {
        QTicket ticket = QTicket.ticket;
        BooleanExpression expressions = filter.getQueryDslExpression();
        JPAQuery<Ticket> query = factory.selectFrom(QTicket.ticket)
                .where(expressions)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(ticket.createdAt.desc());
        return PageableExecutionUtils.getPage(query.fetch(), pageable, query::fetchCount);

    }

    @Override
    public Page<Ticket> findByAuthor_IdAndFilter(Long authorId, SearchTicketFilter filter, Pageable pageable) {
        QTicket ticket = QTicket.ticket;
        BooleanExpression expressions = Expressions.asBoolean(ticket.author.id.eq(authorId));
        expressions = expressions.and(filter.getQueryDslExpression());

        JPAQuery<Ticket> query = factory.selectFrom(QTicket.ticket)
                .where(expressions);
        Long size =Long.valueOf( query.fetch().size());

        List<Ticket> fetched = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(ticket.createdAt.desc())
                .fetch();
        return PageableExecutionUtils.getPage(fetched, pageable, () -> size);
    }
}
