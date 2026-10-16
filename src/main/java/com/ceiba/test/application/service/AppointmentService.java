package com.ceiba.test.application.service;

import com.ceiba.test.application.dto.AppointmentRequest;
import com.ceiba.test.application.dto.AppointmentResponse;
import com.ceiba.test.application.dto.AppointmentSearchCriteria;
import com.ceiba.test.application.dto.CancellationResponse;
import com.ceiba.test.application.dto.RescheduleRequest;
import com.ceiba.test.application.dto.RescheduleResponse;
import com.ceiba.test.application.dto.TimeSlotResponse;
import com.ceiba.test.application.usecase.AppointmentUseCase;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.InvalidDataException;
import com.ceiba.test.domain.exception.ResourceNotFoundException;
import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.AppointmentFilter;
import com.ceiba.test.domain.model.Patient;
import com.ceiba.test.domain.repository.AppointmentRepository;
import com.ceiba.test.domain.repository.DoctorRepository;
import com.ceiba.test.domain.repository.PatientRepository;
import com.ceiba.test.domain.service.AppointmentScheduler;
import com.ceiba.test.domain.service.AvailabilityCalculator;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@AllArgsConstructor
public class AppointmentService implements AppointmentUseCase {

    static final int MAX_AVAILABILITY_RANGE_DAYS = 31;

    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final AppointmentScheduler scheduler;
    private final AvailabilityCalculator availability;
    private final Clock clock;

    @Override
    public AppointmentResponse create(AppointmentRequest request) {
        Patient patient = findPatient(request.pacienteId());
        ensureDoctorExists(request.medicoId());
        Appointment appointment = scheduler.create(
                patient,
                request.medicoId(),
                request.fechaHora(),
                now()
        );
        return AppointmentResponse.from(appointment);
    }

    @Override
    public CancellationResponse cancel(Long appointmentId) {
        Appointment appointment = findAppointment(appointmentId);
        return CancellationResponse.from(scheduler.cancel(appointment, now()));
    }

    @Override
    public RescheduleResponse reschedule(Long appointmentId, RescheduleRequest request) {
        Appointment appointment = findAppointment(appointmentId);
        Patient patient = findPatient(appointment.getPatientId());
        return RescheduleResponse.from(
                scheduler.reschedule(appointment, patient, request.nuevaFechaHora(), now()));
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getById(Long appointmentId) {
        return AppointmentResponse.from(findAppointment(appointmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentResponse> search(AppointmentSearchCriteria criteria) {
        validateOptionalRange(criteria.fechaInicio(), criteria.fechaFin());
        AppointmentFilter filter = new AppointmentFilter(
                criteria.medicoId(), criteria.pacienteId(), criteria.estado(),
                criteria.fechaInicio() == null ? null : criteria.fechaInicio().atStartOfDay(),
                criteria.fechaFin() == null ? null : criteria.fechaFin().plusDays(1).atStartOfDay());
        return appointments.search(filter).stream().map(AppointmentResponse::from).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlotResponse> findAvailableSlots(Long doctorId, LocalDate fromDate, LocalDate toDate) {
        ensureDoctorExists(doctorId);
        validateOptionalRange(fromDate, toDate);
        if (ChronoUnit.DAYS.between(fromDate, toDate) >= MAX_AVAILABILITY_RANGE_DAYS) {
            throw new InvalidDataException(ErrorCode.INVALID_DATE_RANGE,
                    "El rango de consulta no puede superar los %d días".formatted(MAX_AVAILABILITY_RANGE_DAYS));
        }
        return availability.availableSlots(doctorId, fromDate, toDate, now()).stream()
                .map(TimeSlotResponse::from)
                .toList();
    }

    private void validateOptionalRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new InvalidDataException(ErrorCode.INVALID_DATE_RANGE,
                    "fechaInicio no puede ser posterior a fechaFin");
        }
    }

    private Appointment findAppointment(Long id) {
        return appointments.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.APPOINTMENT_NOT_FOUND, "No existe una cita con id " + id));
    }

    private Patient findPatient(Long id) {
        return patients.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                ErrorCode.PATIENT_NOT_FOUND, "No existe un paciente con id " + id));
    }

    private void ensureDoctorExists(Long id) {
        if (doctors.findById(id).isEmpty()) {
            throw new ResourceNotFoundException(
                    ErrorCode.DOCTOR_NOT_FOUND,
                    "No existe un médico con id " + id
            );
        }
    }

    private LocalDateTime now() {

        return LocalDateTime.now(clock);
    }
}
