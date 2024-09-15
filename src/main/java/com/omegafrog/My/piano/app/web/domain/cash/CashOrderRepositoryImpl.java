package com.omegafrog.My.piano.app.web.domain.cash;

import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.web.dto.dateRange.DateRange;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CashOrderRepositoryImpl implements CashOrderRepositoryCustom {

    private static final int EXPIRATION_MINUTE = 30;

    private final JPAQueryFactory jpaQueryFactory;
    private final QCashOrder qCashOrder = QCashOrder.cashOrder;


    /**
     * CashOrder를 userId로 검색<br/>
     * 최신 날짜순으로 정렬됨
     *
     * @param userId
     * @param pageable
     * @return {@link List}<{@link CashOrder}>
     */
    @Transactional
    public Page<CashOrder> findByUserId(Long userId, Pageable pageable) {
        JPAQuery<CashOrder> query = jpaQueryFactory.select(qCashOrder)
                .from(qCashOrder)
                .where(qCashOrder.userId.eq(userId));
        List<CashOrder> fetched = query
                .orderBy(qCashOrder.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return PageableExecutionUtils.getPage(fetched, pageable, ()->query.fetchAll().stream().count());
    }

    /**
     * userId와 Date 범위로  CashOrder를 조회한다
     *
     * @param userId 결제한 user의 id
     * @param pageable {@link Pageable}
     * @param range  {@link  DateRange}의 구현체
     * @return {@link List}<{@link CashOrder}>
     */
    @Transactional
    public Page<CashOrder> findByUserIdAndDate(Long userId, Pageable pageable, DateRange range) {
        LocalDateTime start =range.getStart().atStartOfDay();
        LocalDateTime end = range.getEnd().atTime(23, 59, 59);
        JPAQuery<CashOrder> query = jpaQueryFactory.select(qCashOrder)
                .from(qCashOrder)
                .where(qCashOrder.userId.eq(userId),
                        qCashOrder.createdAt.between(start, end));
        List<CashOrder> fetched = query
                .orderBy(qCashOrder.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
        return PageableExecutionUtils.getPage(fetched, pageable, ()->query.fetchAll().stream().count());
    }

    @Transactional
    @Override
    public List<CashOrder> findExpired(DateRange range) {
        LocalDateTime start =range.getStart().atStartOfDay();
        LocalDateTime end = range.getEnd().atTime(23, 59, 59);
        return jpaQueryFactory.select(qCashOrder)
                .from(qCashOrder)
                .where(isExpired(),
                        qCashOrder.createdAt.between(start, end))
                .orderBy(qCashOrder.createdAt.desc())
                .fetch();
    }

    @Override
    public List<CashOrder> findExpired(Long userId, DateRange range) {
        LocalDateTime start =range.getStart().atStartOfDay();
        LocalDateTime end = range.getEnd().atTime(23, 59, 59);
        return jpaQueryFactory.select(qCashOrder)
                .from(qCashOrder)
                .where(isExpired(),
                        qCashOrder.userId.eq(userId),
                        qCashOrder.createdAt.between(start, end))
                .orderBy(qCashOrder.createdAt.desc())
                .fetch();
    }

    // READY, IN_PROGRESS 이면서 유효시간이 지났는지 확인
    private BooleanExpression isExpired(){
        LocalDateTime current = LocalDateTime.now();
        return qCashOrder.createdAt.before(current.minusMinutes(EXPIRATION_MINUTE))
                .and(qCashOrder.status.eq(OrderStatus.IN_PROGRESS).or(qCashOrder.status.eq(OrderStatus.READY)));
    }
}
