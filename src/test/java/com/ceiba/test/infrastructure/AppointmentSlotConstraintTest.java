package com.ceiba.test.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ceiba.test.domain.exception.BusinessConflictException;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.repository.AppointmentRepository;
import com.ceiba.test.support.ApiIntegrationTest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AppointmentSlotConstraintTest extends ApiIntegrationTest {

    private static final LocalDateTime SLOT = LocalDateTime.of(2026, 6, 2, 9, 0);

    @Autowired
    private AppointmentRepository repository;

    @Test
    void databaseRejectsTwoScheduledAppointmentsForTheSameDoctorSlot() throws Exception {
        long doctor = createDoctor();
        repository.save(Appointment.schedule(createPatient(), doctor, SLOT, SLOT.minusDays(1), null));
        long otherPatient = createPatient();

        assertThatThrownBy(() -> repository.save(
                Appointment.schedule(otherPatient, doctor, SLOT, SLOT.minusDays(1), null)))
                .isInstanceOf(BusinessConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.DOCTOR_SLOT_TAKEN);
    }

    @Test
    void databaseRejectsTwoScheduledAppointmentsForTheSamePatientSlot() throws Exception {
        long patient = createPatient();
        repository.save(Appointment.schedule(patient, createDoctor(), SLOT, SLOT.minusDays(1), null));
        long otherDoctor = createDoctor();

        assertThatThrownBy(() -> repository.save(
                Appointment.schedule(patient, otherDoctor, SLOT, SLOT.minusDays(1), null)))
                .isInstanceOf(BusinessConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.PATIENT_SLOT_TAKEN);
    }

    @Test
    void cancelledAppointmentsDoNotHoldTheSlot() throws Exception {
        long doctor = createDoctor();
        long patient = createPatient();
        Appointment first = repository.save(Appointment.schedule(patient, doctor, SLOT, SLOT.minusDays(1), null));
        first.cancel(SLOT.minusDays(1));
        repository.save(first);

        Appointment second = repository.save(Appointment.schedule(patient, doctor, SLOT, SLOT.minusDays(1), null));

        assertThat(second.getId()).isNotEqualTo(first.getId());
    }
}
