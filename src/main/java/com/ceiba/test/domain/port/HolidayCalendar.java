package com.ceiba.test.domain.port;

import java.time.LocalDate;

public interface HolidayCalendar {

    boolean isHoliday(LocalDate date);
}
