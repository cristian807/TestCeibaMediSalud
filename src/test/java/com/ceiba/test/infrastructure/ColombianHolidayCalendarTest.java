package com.ceiba.test.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.ceiba.test.infrastructure.calendar.ColombianHolidayCalendar;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ColombianHolidayCalendarTest {

    private final ColombianHolidayCalendar calendar = new ColombianHolidayCalendar();

    @ParameterizedTest
    @ValueSource(strings = {
            "2026-01-01", "2026-01-12", "2026-03-23", "2026-04-02", "2026-04-03", "2026-05-01", "2026-05-18",
            "2026-06-08", "2026-06-15", "2026-06-29", "2026-07-20", "2026-08-07", "2026-08-17", "2026-10-12",
            "2026-11-02", "2026-11-16", "2026-12-08", "2026-12-25"})
    void recognizesColombianHolidaysOf2026(String date) {
        assertThat(calendar.isHoliday(LocalDate.parse(date))).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-01-06", "2026-03-19", "2026-06-02", "2026-06-10", "2026-11-01", "2026-12-24"})
    void ordinaryDaysAreNotHolidays(String date) {
        assertThat(calendar.isHoliday(LocalDate.parse(date))).isFalse();
    }

    @Test
    void holidaysAreComputedForAnyYear() {
        assertThat(calendar.isHoliday(LocalDate.of(2027, 3, 26))).isTrue();
        assertThat(calendar.isHoliday(LocalDate.of(2025, 4, 18))).isTrue();
    }
}
