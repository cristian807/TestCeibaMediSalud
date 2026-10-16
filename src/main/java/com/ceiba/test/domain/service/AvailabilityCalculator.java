package com.ceiba.test.domain.service;

import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.TimeSlot;
import com.ceiba.test.domain.repository.AppointmentRepository;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@AllArgsConstructor
public class AvailabilityCalculator {

    private final WorkingHoursPolicy workingHours;
    private final AppointmentRepository appointments;

    public List<TimeSlot> availableSlots(
            Long doctorId,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDateTime now
    ) {
        Set<LocalDateTime> occupied = appointments.findScheduledStartTimes(
                doctorId,
                fromDate.atStartOfDay(),
                toDate.plusDays(1).atStartOfDay()
        );

        List<TimeSlot> available = new ArrayList<>();

        for (LocalDate day = fromDate; !day.isAfter(toDate); day = day.plusDays(1)) {
            for (LocalDateTime start : workingHours.slotStarts(day)) {
                if (start.isAfter(now) && !occupied.contains(start)) {
                    available.add(new TimeSlot(start, start.plus(Appointment.SLOT_DURATION)));
                }
            }
        }
        return available;
    }
}
