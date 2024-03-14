package com.omegafrog.My.piano.app.web.dto.dateRange;

import com.omegafrog.My.piano.app.web.enums.DateRangeType;

import java.time.LocalDate;

public interface DateRangeFactory {
    public DateRange calcDateRange(DateRangeType range);

    DateRange calcDateRange(LocalDate start, LocalDate end);
}
