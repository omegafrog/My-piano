package com.omegafrog.My.piano.app.web.service.ranking;

import com.omegafrog.My.piano.app.web.enums.DateRangeType;

public interface DateRangeFactory {
    public DateRange calcDateRange(DateRangeType range);
}
