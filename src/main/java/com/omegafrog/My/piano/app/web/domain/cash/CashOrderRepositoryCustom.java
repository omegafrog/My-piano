package com.omegafrog.My.piano.app.web.domain.cash;

import com.omegafrog.My.piano.app.web.dto.dateRange.DateRange;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CashOrderRepositoryCustom {
    Page<CashOrder> findByUserId(Long userId, Pageable pageable);

    Page<CashOrder> findByUserIdAndDate(Long userId, Pageable pageable, DateRange range);

    public List<CashOrder> findExpired(DateRange range);
    public List<CashOrder> findExpired(Long userId, DateRange range);
}
