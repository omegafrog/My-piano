package com.omegafrog.My.piano.app.web.dto.dateRange;

import com.omegafrog.My.piano.app.web.enums.DateRangeType;

import java.time.LocalDate;

public class MonthlyDateRange extends DateRange {
    public MonthlyDateRange(LocalDate start, LocalDate end) {
        super(start, end, DateRangeType.MONTHLY);
    }
}
