package com.ceiba.test.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.ceiba.test.application.dto.AppointmentRequest;
import com.ceiba.test.application.dto.AppointmentSearchCriteria;
import com.ceiba.test.application.service.AppointmentService;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.InvalidDataException;
import com.ceiba.test.domain.exception.ResourceNotFoundException;
import com.ceiba.test.domain.model.AppointmentFilter;
import com.ceiba.test.domain.model.AppointmentStatus;
import com.ceiba.test.domain.model.Doctor;
import com.ceiba.test.domain.model.Patient;
import com.ceiba.test.domain.repository.AppointmentRepository;
import com.ceiba.test.domain.repository.DoctorRepository;
import com.ceiba.test.domain.repository.PatientRepository;
import com.ceiba.test.domain.service.AppointmentScheduler;
import com.ceiba.test.domain.service.AvailabilityCalculator;
import com.ceiba.test.support.TestClocks;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class AppointmentServiceTest {

    private AppointmentRepository appointments;
    private DoctorRepository doctors;
    private PatientRepository patients;
    private AppointmentScheduler scheduler;
    private AvailabilityCalculator availability;
    private AppointmentService service;

    @BeforeEach
    void setUp() {
        appointments = mock(AppointmentRepository.class);
        doctors = mock(DoctorRepository.class);
        patients = mock(PatientRepository.class);
        scheduler = mock(AppointmentScheduler.class);
        availability = mock(AvailabilityCalculator.class);
        service = new AppointmentService(appointments, doctors, patients, scheduler, availability,
                TestClocks.fixed());
    }

    @Test
    void bookFailsWhenPatientDoesNotExist() {
        when(patients.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new AppointmentRequest(7L, 1L, TestClocks.NOW.plusDays(1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.PATIENT_NOT_FOUND);
        verifyNoInteractions(scheduler);
    }

    @Test
    void bookFailsWhenDoctorDoesNotExist() {
        when(patients.findById(7L)).thenReturn(Optional.of(
                Patient.restore(7L, "Paciente Uno", "1234567", "5551234", "p@mail.com", null)));
        when(doctors.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(new AppointmentRequest(7L, 9L, TestClocks.NOW.plusDays(1))))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.DOCTOR_NOT_FOUND);
        verifyNoInteractions(scheduler);
    }

    @Test
    void cancelFailsWhenAppointmentDoesNotExist() {
        when(appointments.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancel(5L))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.APPOINTMENT_NOT_FOUND);
    }

    @Test
    void availabilityRejectsInvertedRange() {
        when(doctors.findById(1L)).thenReturn(Optional.of(Doctor.restore(1L, "Dr. Uno", "Pediatría", null, null)));

        assertThatThrownBy(() -> service.findAvailableSlots(1L, LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 9)))
                .isInstanceOf(InvalidDataException.class)
                .extracting("code").isEqualTo(ErrorCode.INVALID_DATE_RANGE);
        verifyNoInteractions(availability);
    }

    @Test
    void availabilityRejectsRangesLongerThanOneMonth() {
        when(doctors.findById(1L)).thenReturn(Optional.of(Doctor.restore(1L, "Dr. Uno", "Pediatría", null, null)));

        assertThatThrownBy(() -> service.findAvailableSlots(1L, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 8, 1)))
                .isInstanceOf(InvalidDataException.class)
                .extracting("code").isEqualTo(ErrorCode.INVALID_DATE_RANGE);
    }

    @Test
    void availabilityFailsForUnknownDoctor() {
        when(doctors.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findAvailableSlots(1L, LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 2)))
                .isInstanceOf(ResourceNotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.DOCTOR_NOT_FOUND);
    }

    @Test
    void searchConvertsInclusiveDatesIntoHalfOpenDateTimeRange() {
        when(appointments.search(any())).thenReturn(List.of());

        service.search(new AppointmentSearchCriteria(1L, 2L, AppointmentStatus.PROGRAMADA,
                LocalDate.of(2026, 6, 2), LocalDate.of(2026, 6, 3)));

        ArgumentCaptor<AppointmentFilter> captor = ArgumentCaptor.forClass(AppointmentFilter.class);
        verify(appointments).search(captor.capture());
        AppointmentFilter filter = captor.getValue();
        assertThat(filter.doctorId()).isEqualTo(1L);
        assertThat(filter.patientId()).isEqualTo(2L);
        assertThat(filter.status()).isEqualTo(AppointmentStatus.PROGRAMADA);
        assertThat(filter.from()).isEqualTo(LocalDateTime.of(2026, 6, 2, 0, 0));
        assertThat(filter.to()).isEqualTo(LocalDateTime.of(2026, 6, 4, 0, 0));
    }

    @Test
    void searchRejectsInvertedRange() {
        assertThatThrownBy(() -> service.search(new AppointmentSearchCriteria(null, null, null,
                LocalDate.of(2026, 6, 5), LocalDate.of(2026, 6, 1))))
                .isInstanceOf(InvalidDataException.class)
                .extracting("code").isEqualTo(ErrorCode.INVALID_DATE_RANGE);
    }
}
