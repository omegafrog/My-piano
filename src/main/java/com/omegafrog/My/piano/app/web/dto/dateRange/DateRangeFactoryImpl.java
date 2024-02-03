package com.omegafrog.My.piano.app.web.dto.dateRange;

import com.omegafrog.My.piano.app.web.enums.DateRangeType;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;

@Component
public class DateRangeFactoryImpl implements DateRangeFactory {
    @Override
    public DateRange calcDateRange(DateRangeType range) {
        LocalDate today = LocalDate.now();
        switch (range){
            case WEEKLY -> {

                // 오늘로부터 이번 주의 시작 날짜 계산 (월요일 기준)
                LocalDate startOfWeek = today.minusDays(Integer.toUnsignedLong(today.getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue()));

                // 이번 주의 끝 날짜 계산 (일요일 기준)
                LocalDate endOfWeek = startOfWeek.plusDays(6);
                return new WeeklyDateRange(startOfWeek, endOfWeek);
            }
            case MONTHLY ->{

                // 오늘로부터 이번 달의 시작 날짜
                LocalDate startOfMonth = LocalDate.of(today.getYear(), today.getMonth(),1);

                // 이번 달의 끝 날짜
                LocalDate endOfMonth = LocalDate.of(today.getYear(), today.getMonth(), today.lengthOfMonth());
                return new MonthlyDateRange(startOfMonth, endOfMonth);
            }
            default -> {
                throw new IllegalArgumentException("Wrong DateRangeType");
            }
        }
    }
    public DateRange calcDateRange(LocalDate start, LocalDate end){
        return new CustomDateRange(start, end);
    }
}
