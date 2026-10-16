package com.ceiba.test.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ceiba.test.domain.exception.BusinessConflictException;
import com.ceiba.test.domain.exception.DomainException;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.InvalidDataException;
import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.AppointmentStatus;
import com.ceiba.test.domain.model.Patient;
import com.ceiba.test.domain.service.AppointmentScheduler;
import com.ceiba.test.domain.service.CancellationResult;
import com.ceiba.test.domain.service.RescheduleResult;
import com.ceiba.test.domain.service.WorkingHoursPolicy;
import com.ceiba.test.support.InMemoryAppointmentRepository;
import com.ceiba.test.support.InMemoryPenaltyRepository;
import com.ceiba.test.support.TestClocks;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AppointmentSchedulerTest {

    private static final LocalDateTime NOW = TestClocks.NOW;
    private static final LocalDateTime TUESDAY_9 = LocalDateTime.of(2026, 6, 2, 9, 0);
    private static final Long DOCTOR = 1L;

    private InMemoryAppointmentRepository appointments;
    private InMemoryPenaltyRepository penalties;
    private AppointmentScheduler scheduler;
    private Patient patient;

    @BeforeEach
    void setUp() {
        appointments = new InMemoryAppointmentRepository();
        penalties = new InMemoryPenaltyRepository();
        WorkingHoursPolicy workingHours = new WorkingHoursPolicy(date -> date.equals(LocalDate.of(2026, 6, 8)));
        scheduler = new AppointmentScheduler(appointments, penalties, workingHours, 3, 30);
        patient = Patient.restore(10L, "Paciente Uno", "1234567", "5551234", "p1@mail.com", null);
    }

    private static void assertRejected(Runnable action, Class<? extends DomainException> type, ErrorCode code) {
        assertThatThrownBy(action::run).isInstanceOf(type).extracting("code").isEqualTo(code);
    }

    @Test
    void booksAScheduledAppointment() {
        Appointment booked = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);

        assertThat(booked.getId()).isNotNull();
        assertThat(booked.getStatus()).isEqualTo(AppointmentStatus.PROGRAMADA);
        assertThat(booked.getStartTime()).isEqualTo(TUESDAY_9);
    }

    @Test
    void rejectsBookingOnHolidayAndSunday() {
        assertRejected(() -> scheduler.book(patient, DOCTOR, LocalDateTime.of(2026, 6, 8, 9, 0), NOW),
                InvalidDataException.class, ErrorCode.NON_WORKING_DAY);
        assertRejected(() -> scheduler.book(patient, DOCTOR, LocalDateTime.of(2026, 6, 7, 9, 0), NOW),
                InvalidDataException.class, ErrorCode.NON_WORKING_DAY);
    }

    @Test
    void rejectsBookingOnSaturdayAfternoon() {
        assertRejected(() -> scheduler.book(patient, DOCTOR, LocalDateTime.of(2026, 6, 6, 13, 0), NOW),
                InvalidDataException.class, ErrorCode.OUTSIDE_WORKING_HOURS);
    }

    @Test
    void rejectsBookingInThePast() {
        assertRejected(() -> scheduler.book(patient, DOCTOR, LocalDateTime.of(2026, 6, 1, 9, 30), NOW),
                InvalidDataException.class, ErrorCode.APPOINTMENT_IN_THE_PAST);
        assertRejected(() -> scheduler.book(patient, DOCTOR, NOW, NOW),
                InvalidDataException.class, ErrorCode.APPOINTMENT_IN_THE_PAST);
    }

    @Test
    void rejectsSecondAppointmentForTheSameDoctorSlot() {
        scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);
        Patient other = Patient.restore(11L, "Paciente Dos", "7654321", "5554321", "p2@mail.com", null);

        assertRejected(() -> scheduler.book(other, DOCTOR, TUESDAY_9, NOW),
                BusinessConflictException.class, ErrorCode.DOCTOR_SLOT_TAKEN);
    }

    @Test
    void rejectsPatientDoubleBookingEvenWithAnotherDoctor() {
        scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);

        assertRejected(() -> scheduler.book(patient, 2L, TUESDAY_9, NOW),
                BusinessConflictException.class, ErrorCode.PATIENT_SLOT_TAKEN);
    }

    @Test
    void adjacentSlotsAreIndependent() {
        scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);

        Appointment next = scheduler.book(patient, DOCTOR, TUESDAY_9.plusMinutes(30), NOW);

        assertThat(next.getStartTime()).isEqualTo(TUESDAY_9.plusMinutes(30));
    }

    @Test
    void cancelledSlotCanBeBookedAgain() {
        Appointment first = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);
        scheduler.cancel(first, NOW);

        Appointment again = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);

        assertThat(again.getId()).isNotEqualTo(first.getId());
    }

    @Test
    void rejectsPatientWithFutureBirthDate() {
        Patient unborn = Patient.restore(12L, "Paciente Tres", "1111111", "5551111", "p3@mail.com",
                NOW.toLocalDate().plusDays(1));

        assertRejected(() -> scheduler.book(unborn, DOCTOR, TUESDAY_9, NOW),
                InvalidDataException.class, ErrorCode.INVALID_BIRTH_DATE);
    }

    @Test
    void acceptsPatientBornToday() {
        Patient newborn = Patient.restore(12L, "Paciente Tres", "1111111", "5551111", "p3@mail.com",
                NOW.toLocalDate());

        assertThat(scheduler.book(newborn, DOCTOR, TUESDAY_9, NOW)).isNotNull();
    }

    @Test
    void lateCancellationRegistersPenalty() {
        Appointment booked = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);

        CancellationResult result = scheduler.cancel(booked, TUESDAY_9.minusMinutes(90));

        assertThat(result.penaltyApplied()).isTrue();
        assertThat(result.appointment().getStatus()).isEqualTo(AppointmentStatus.CANCELADA);
        assertThat(penalties.size()).isEqualTo(1);
    }

    @Test
    void earlyCancellationHasNoPenalty() {
        Appointment booked = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);

        CancellationResult result = scheduler.cancel(booked, TUESDAY_9.minusHours(2));

        assertThat(result.penaltyApplied()).isFalse();
        assertThat(penalties.size()).isZero();
    }

    @Test
    void patientWithThreeRecentPenaltiesCannotBook() {
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(1));
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(10));
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(29));

        assertRejected(() -> scheduler.book(patient, DOCTOR, TUESDAY_9, NOW),
                BusinessConflictException.class, ErrorCode.PATIENT_BLOCKED_BY_PENALTIES);
    }

    @Test
    void patientWithTwoPenaltiesCanStillBook() {
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(1));
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(2));

        assertThat(scheduler.book(patient, DOCTOR, TUESDAY_9, NOW)).isNotNull();
    }

    @Test
    void penaltiesOlderThanThirtyDaysDoNotCount() {
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(1));
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(2));
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(31));

        assertThat(scheduler.book(patient, DOCTOR, TUESDAY_9, NOW)).isNotNull();
    }

    @Test
    void penaltiesOfOtherPatientsDoNotBlock() {
        penalties.addPenaltyAt(99L, NOW.minusDays(1));
        penalties.addPenaltyAt(99L, NOW.minusDays(2));
        penalties.addPenaltyAt(99L, NOW.minusDays(3));

        assertThat(scheduler.book(patient, DOCTOR, TUESDAY_9, NOW)).isNotNull();
    }

    @Test
    void rescheduleCancelsOldAppointmentAndCreatesNewOne() {
        Appointment original = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);
        LocalDateTime newStart = LocalDateTime.of(2026, 6, 3, 11, 0);

        RescheduleResult result = scheduler.reschedule(original, patient, newStart, NOW);

        assertThat(result.cancelled().getStatus()).isEqualTo(AppointmentStatus.CANCELADA);
        assertThat(result.created().getStatus()).isEqualTo(AppointmentStatus.PROGRAMADA);
        assertThat(result.created().getStartTime()).isEqualTo(newStart);
        assertThat(result.created().getDoctorId()).isEqualTo(DOCTOR);
        assertThat(result.created().getRescheduledFromId()).isEqualTo(original.getId());
        assertThat(result.penaltyApplied()).isFalse();
        assertThat(appointments.existsScheduledByDoctorAt(DOCTOR, TUESDAY_9)).isFalse();
    }

    @Test
    void lateRescheduleAppliesPenalty() {
        Appointment original = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);

        RescheduleResult result = scheduler.reschedule(original, patient,
                LocalDateTime.of(2026, 6, 3, 11, 0), TUESDAY_9.minusMinutes(30));

        assertThat(result.penaltyApplied()).isTrue();
        assertThat(penalties.size()).isEqualTo(1);
    }

    @Test
    void rescheduleToOccupiedSlotIsRejected() {
        Appointment original = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);
        Patient other = Patient.restore(11L, "Paciente Dos", "7654321", "5554321", "p2@mail.com", null);
        LocalDateTime taken = LocalDateTime.of(2026, 6, 3, 11, 0);
        scheduler.book(other, DOCTOR, taken, NOW);

        assertRejected(() -> scheduler.reschedule(original, patient, taken, NOW),
                BusinessConflictException.class, ErrorCode.DOCTOR_SLOT_TAKEN);
    }

    @Test
    void rescheduleToSameSlotIsRejected() {
        Appointment original = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);

        assertRejected(() -> scheduler.reschedule(original, patient, TUESDAY_9, NOW),
                InvalidDataException.class, ErrorCode.RESCHEDULE_SAME_SLOT);
    }

    @Test
    void rescheduleToInvalidSlotDoesNotCancelOriginal() {
        Appointment original = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);

        assertRejected(() -> scheduler.reschedule(original, patient, LocalDateTime.of(2026, 6, 7, 9, 0), NOW),
                InvalidDataException.class, ErrorCode.NON_WORKING_DAY);
        assertThat(original.isScheduled()).isTrue();
        assertThat(penalties.size()).isZero();
    }

    @Test
    void cannotRescheduleCancelledAppointment() {
        Appointment original = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);
        scheduler.cancel(original, NOW);

        assertRejected(() -> scheduler.reschedule(original, patient, LocalDateTime.of(2026, 6, 3, 11, 0), NOW),
                BusinessConflictException.class, ErrorCode.APPOINTMENT_NOT_CANCELLABLE);
    }

    @Test
    void blockedPatientCannotReschedule() {
        Appointment original = scheduler.book(patient, DOCTOR, TUESDAY_9, NOW);
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(1));
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(2));
        penalties.addPenaltyAt(patient.getId(), NOW.minusDays(3));

        assertRejected(() -> scheduler.reschedule(original, patient, LocalDateTime.of(2026, 6, 3, 11, 0), NOW),
                BusinessConflictException.class, ErrorCode.PATIENT_BLOCKED_BY_PENALTIES);
        assertThat(original.isScheduled()).isTrue();
    }
}
