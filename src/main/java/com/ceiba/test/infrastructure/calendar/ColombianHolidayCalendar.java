package com.ceiba.test.infrastructure.calendar;

import com.ceiba.test.domain.port.HolidayCalendar;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class ColombianHolidayCalendar implements HolidayCalendar {

    private final Map<Integer, Set<LocalDate>> cache = new ConcurrentHashMap<>();

    @Override
    public boolean isHoliday(LocalDate date) {
        return cache.computeIfAbsent(date.getYear(), ColombianHolidayCalendar::holidaysOf).contains(date);
    }

    static Set<LocalDate> holidaysOf(int year) {
        Set<LocalDate> holidays = new HashSet<>();

        holidays.add(LocalDate.of(year, 1, 1));
        holidays.add(LocalDate.of(year, 5, 1));
        holidays.add(LocalDate.of(year, 7, 20));
        holidays.add(LocalDate.of(year, 8, 7));
        holidays.add(LocalDate.of(year, 12, 8));
        holidays.add(LocalDate.of(year, 12, 25));

        holidays.add(nextMonday(LocalDate.of(year, 1, 6)));
        holidays.add(nextMonday(LocalDate.of(year, 3, 19)));
        holidays.add(nextMonday(LocalDate.of(year, 6, 29)));
        holidays.add(nextMonday(LocalDate.of(year, 8, 15)));
        holidays.add(nextMonday(LocalDate.of(year, 10, 12)));
        holidays.add(nextMonday(LocalDate.of(year, 11, 1)));
        holidays.add(nextMonday(LocalDate.of(year, 11, 11)));

        LocalDate easter = easterSunday(year);
        holidays.add(easter.minusDays(3));
        holidays.add(easter.minusDays(2));
        holidays.add(nextMonday(easter.plusDays(39)));
        holidays.add(nextMonday(easter.plusDays(60)));
        holidays.add(nextMonday(easter.plusDays(68)));
        return holidays;
    }

    private static LocalDate nextMonday(LocalDate date) {
        return date.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
    }

    static LocalDate easterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}
