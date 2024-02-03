package com.omegafrog.My.piano.app.web.service.ranking;


import com.omegafrog.My.piano.app.web.enums.DateRangeType;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public abstract class DateRange {
    private LocalDate start;
    private LocalDate end;

    private DateRangeType range;

    public DateRange(LocalDate start, LocalDate end, DateRangeType range) {
        this.start = start;
        this.end = end;
        this.range = range;
    }
}
