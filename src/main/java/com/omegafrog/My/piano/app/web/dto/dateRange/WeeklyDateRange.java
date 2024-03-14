package com.omegafrog.My.piano.app.web.dto.dateRange;

import com.omegafrog.My.piano.app.web.enums.DateRangeType;

import java.time.LocalDate;

public class WeeklyDateRange extends DateRange {
    public WeeklyDateRange(LocalDate start, LocalDate end) {
        super(start, end, DateRangeType.WEEKLY);
    }
}
