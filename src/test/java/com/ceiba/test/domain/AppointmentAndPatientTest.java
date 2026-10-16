package com.ceiba.test.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ceiba.test.domain.exception.BusinessConflictException;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.AppointmentStatus;
import com.ceiba.test.domain.model.Patient;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class AppointmentAndPatientTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 6, 2, 10, 0);

    private Appointment scheduled() {
        return Appointment.schedule(1L, 1L, START, START.minusDays(1), null);
    }

    @Test
    void newAppointmentIsScheduledAndLastsThirtyMinutes() {
        Appointment appointment = scheduled();

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.PROGRAMADA);
        assertThat(appointment.endTime()).isEqualTo(START.plusMinutes(30));
    }

    @Test
    void cancellationLessThanTwoHoursBeforeIsLate() {
        assertThat(scheduled().isLateCancellation(START.minusHours(2).plusMinutes(1))).isTrue();
        assertThat(scheduled().isLateCancellation(START.plusMinutes(5))).isTrue();
    }

    @Test
    void cancellationTwoHoursOrMoreBeforeIsNotLate() {
        assertThat(scheduled().isLateCancellation(START.minusHours(2))).isFalse();
        assertThat(scheduled().isLateCancellation(START.minusDays(1))).isFalse();
    }

    @Test
    void cancelChangesStatusAndRecordsTimestamp() {
        Appointment appointment = scheduled();
        LocalDateTime now = START.minusDays(1);

        appointment.cancel(now);

        assertThat(appointment.getStatus()).isEqualTo(AppointmentStatus.CANCELADA);
        assertThat(appointment.getCancelledAt()).isEqualTo(now);
    }

    @Test
    void cannotCancelTwice() {
        Appointment appointment = scheduled();
        appointment.cancel(START.minusDays(1));

        assertThatThrownBy(() -> appointment.cancel(START.minusHours(5)))
                .isInstanceOf(BusinessConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.APPOINTMENT_NOT_CANCELLABLE);
    }

    @Test
    void cannotCancelAttendedAppointment() {
        Appointment attended = Appointment.restore(5L, 1L, 1L, START, AppointmentStatus.ATENDIDA, null,
                START.minusDays(2), null);

        assertThatThrownBy(() -> attended.cancel(START.plusHours(1)))
                .isInstanceOf(BusinessConflictException.class);
    }

    @Test
    void missingBirthDateIsTreatedAsAgeZero() {
        Patient patient = Patient.register("Juan Pérez", "ab-12345", "5551234", "juan@mail.com", null);

        assertThat(patient.hasValidBirthDate(LocalDate.of(2026, 6, 1))).isTrue();
    }

    @Test
    void futureBirthDateIsInvalidButTodayIsValid() {
        LocalDate today = LocalDate.of(2026, 6, 1);

        assertThat(Patient.register("Juan Pérez", "1234567", "5551234", "j@mail.com", today.plusDays(1))
                .hasValidBirthDate(today)).isFalse();
        assertThat(Patient.register("Juan Pérez", "1234567", "5551234", "j@mail.com", today)
                .hasValidBirthDate(today)).isTrue();
    }

    @Test
    void documentNumberIsNormalized() {
        Patient patient = Patient.register("Juan Pérez", "  ab-12345 ", "5551234", "juan@mail.com", null);

        assertThat(patient.getDocumentNumber()).isEqualTo("AB-12345");
    }
}
