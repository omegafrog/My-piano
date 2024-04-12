package com.omegafrog.My.piano.app.web.converter;

import com.omegafrog.My.piano.app.web.enums.DateRangeType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToDateRangeTypeConverter implements Converter<String, DateRangeType> {
    @Override
    public DateRangeType convert(String source) {
        return DateRangeType.valueOf(source.toUpperCase());
    }
}
