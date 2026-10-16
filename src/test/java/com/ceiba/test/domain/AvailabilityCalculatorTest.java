package com.ceiba.test.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.TimeSlot;
import com.ceiba.test.domain.service.AppointmentScheduler;
import com.ceiba.test.domain.service.AvailabilityCalculator;
import com.ceiba.test.domain.service.WorkingHoursPolicy;
import com.ceiba.test.support.InMemoryAppointmentRepository;
import com.ceiba.test.support.InMemoryPenaltyRepository;
import com.ceiba.test.support.TestClocks;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AvailabilityCalculatorTest {

    private static final LocalDate TUESDAY = LocalDate.of(2026, 6, 2);

    private InMemoryAppointmentRepository appointments;
    private AppointmentScheduler scheduler;
    private AvailabilityCalculator calculator;

    @BeforeEach
    void setUp() {
        appointments = new InMemoryAppointmentRepository();
        WorkingHoursPolicy policy = new WorkingHoursPolicy(date -> date.equals(LocalDate.of(2026, 6, 8)));
        scheduler = new AppointmentScheduler(appointments, new InMemoryPenaltyRepository(), policy, 3, 30);
        calculator = new AvailabilityCalculator(policy, appointments);
    }

    @Test
    void emptyAgendaReturnsEveryThirtyMinuteSlot() {
        List<TimeSlot> slots = calculator.availableSlots(1L, TUESDAY, TUESDAY, TestClocks.NOW);

        assertThat(slots).hasSize(20);
        assertThat(slots.get(0).start()).isEqualTo(TUESDAY.atTime(8, 0));
        assertThat(slots.get(0).end()).isEqualTo(TUESDAY.atTime(8, 30));
    }

    @Test
    void occupiedSlotsAreExcluded() {
        var patient = com.ceiba.test.domain.model.Patient.restore(10L, "Paciente Uno", "1234567", "5551234",
                "p@mail.com", null);
        scheduler.book(patient, 1L, TUESDAY.atTime(9, 0), TestClocks.NOW);

        List<LocalDateTime> starts = calculator.availableSlots(1L, TUESDAY, TUESDAY, TestClocks.NOW).stream()
                .map(TimeSlot::start).toList();

        assertThat(starts).hasSize(19).doesNotContain(TUESDAY.atTime(9, 0));
    }

    @Test
    void cancelledAppointmentsFreeTheirSlot() {
        var patient = com.ceiba.test.domain.model.Patient.restore(10L, "Paciente Uno", "1234567", "5551234",
                "p@mail.com", null);
        Appointment booked = scheduler.book(patient, 1L, TUESDAY.atTime(9, 0), TestClocks.NOW);
        scheduler.cancel(booked, TestClocks.NOW);

        assertThat(calculator.availableSlots(1L, TUESDAY, TUESDAY, TestClocks.NOW)).hasSize(20);
    }

    @Test
    void otherDoctorsAppointmentsDoNotAffectAvailability() {
        var patient = com.ceiba.test.domain.model.Patient.restore(10L, "Paciente Uno", "1234567", "5551234",
                "p@mail.com", null);
        scheduler.book(patient, 2L, TUESDAY.atTime(9, 0), TestClocks.NOW);

        assertThat(calculator.availableSlots(1L, TUESDAY, TUESDAY, TestClocks.NOW)).hasSize(20);
    }

    @Test
    void rangeSkipsSundaysAndHolidays() {
        List<TimeSlot> slots = calculator.availableSlots(1L, LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 9),
                TestClocks.NOW);

        assertThat(slots).hasSize(50);
    }

    @Test
    void pastSlotsOfTodayAreNotOffered() {
        LocalDate today = TestClocks.NOW.toLocalDate();

        List<TimeSlot> slots = calculator.availableSlots(1L, today, today, TestClocks.NOW);

        assertThat(slots).hasSize(15);
        assertThat(slots.get(0).start()).isEqualTo(today.atTime(10, 30));
    }
}
