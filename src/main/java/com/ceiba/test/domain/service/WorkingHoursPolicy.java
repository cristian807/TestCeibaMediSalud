package com.ceiba.test.domain.service;

import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.InvalidDataException;
import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.port.HolidayCalendar;
import lombok.AllArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor
public class WorkingHoursPolicy {

    private static final LocalTime OPENING = LocalTime.of(8, 0);
    private static final LocalTime WEEKDAY_CLOSING = LocalTime.of(18, 0);
    private static final LocalTime SATURDAY_CLOSING = LocalTime.of(13, 0);

    private final HolidayCalendar holidayCalendar;

    public Optional<LocalTime> closingTime(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        if (day == DayOfWeek.SUNDAY || holidayCalendar.isHoliday(date)) {
            return Optional.empty();
        }
        return Optional.of(day == DayOfWeek.SATURDAY ? SATURDAY_CLOSING : WEEKDAY_CLOSING);
    }

    public List<LocalDateTime> slotStarts(LocalDate date) {
        List<LocalDateTime> starts = new ArrayList<>();
        closingTime(date).ifPresent(closing -> {
            LocalTime cursor = OPENING;
            while (!cursor.plus(Appointment.SLOT_DURATION).isAfter(closing)) {
                starts.add(date.atTime(cursor));
                cursor = cursor.plus(Appointment.SLOT_DURATION);
            }
        });
        return starts;
    }

    public void validateSlot(LocalDateTime start) {
        boolean aligned = start.getSecond() == 0 && start.getNano() == 0
                && (start.getMinute() == 0 || start.getMinute() == 30);
        if (!aligned) {
            throw new InvalidDataException(ErrorCode.INVALID_SLOT_ALIGNMENT,
                    "Las citas deben iniciar en punto o y media (franjas de 30 minutos)");
        }
        LocalDate date = start.toLocalDate();
        LocalTime closing = closingTime(date).orElseThrow(() -> new InvalidDataException(
                ErrorCode.NON_WORKING_DAY, "No hay atención los domingos ni festivos: " + date));
        LocalTime time = start.toLocalTime();
        if (time.isBefore(OPENING) || time.plus(Appointment.SLOT_DURATION).isAfter(closing)) {
            throw new InvalidDataException(ErrorCode.OUTSIDE_WORKING_HOURS,
                    "El horario de atención del %s es de %s a %s".formatted(date, OPENING, closing));
        }
    }
}
