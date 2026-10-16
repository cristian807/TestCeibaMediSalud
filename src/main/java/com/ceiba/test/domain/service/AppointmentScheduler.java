package com.ceiba.test.domain.service;

import com.ceiba.test.domain.exception.BusinessConflictException;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.InvalidDataException;
import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.Patient;
import com.ceiba.test.domain.model.Penalty;
import com.ceiba.test.domain.repository.AppointmentRepository;
import com.ceiba.test.domain.repository.PenaltyRepository;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class AppointmentScheduler {

    private final AppointmentRepository appointments;
    private final PenaltyRepository penalties;
    private final WorkingHoursPolicy workingHours;
    private final int maxPenalties;
    private final int penaltyWindowDays;

    public Appointment create(
            Patient patient,
            Long doctorId,
            LocalDateTime start,
            LocalDateTime now
    ) {
        ensurePatientCanBook(patient, now);
        validateRequestedSlot(start, now);
        ensureSlotIsFree(patient.getId(), doctorId, start);
        return appointments.save(Appointment.schedule(
                patient.getId(),
                doctorId,
                start,
                now,
                null)
        );
    }

    public CancellationResult cancel(Appointment appointment, LocalDateTime now) {
        boolean late = appointment.isLateCancellation(now);
        appointment.cancel(now);
        Appointment cancelled = appointments.save(appointment);
        if (late) {
            penalties.save(Penalty.issue(
                    cancelled.getPatientId(),
                    cancelled.getId(),
                    now)
            );
        }
        return new CancellationResult(cancelled, late);
    }

    public RescheduleResult reschedule(
            Appointment appointment,
            Patient patient,
            LocalDateTime newStart,
            LocalDateTime now
    ) {
        if (appointment.isScheduled() && newStart.equals(appointment.getStartTime())) {
            throw new InvalidDataException(ErrorCode.RESCHEDULE_SAME_SLOT,
                    "El nuevo horario debe ser diferente al horario actual de la cita");
        }
        ensurePatientCanBook(patient, now);
        validateRequestedSlot(newStart, now);

        CancellationResult cancellation = cancel(appointment, now);
        ensureSlotIsFree(appointment.getPatientId(), appointment.getDoctorId(), newStart);

        Appointment created = appointments.save(Appointment.schedule(
                appointment.getPatientId(),
                appointment.getDoctorId(),
                newStart,
                now,
                appointment.getId()
        ));
        return new RescheduleResult(cancellation.appointment(), created, cancellation.penaltyApplied());
    }

    private void ensurePatientCanBook(Patient patient, LocalDateTime now) {
        if (!patient.hasValidBirthDate(now.toLocalDate())) {
            throw new InvalidDataException(ErrorCode.INVALID_BIRTH_DATE,
                    "La fecha de nacimiento del paciente no puede ser futura");
        }
        long recentPenalties = penalties.countByPatientSince(
                patient.getId(),
                now.minusDays(penaltyWindowDays)
        );
        if (recentPenalties >= maxPenalties) {
            throw new BusinessConflictException(ErrorCode.PATIENT_BLOCKED_BY_PENALTIES,
                    "El paciente tiene %d penalizaciones en los últimos %d días y no puede agendar nuevas citas"
                            .formatted(recentPenalties, penaltyWindowDays));
        }
    }

    private void validateRequestedSlot(LocalDateTime start, LocalDateTime now) {
        workingHours.validateSlot(start);
        if (!start.isAfter(now)) {
            throw new InvalidDataException(ErrorCode.APPOINTMENT_IN_THE_PAST,
                    "No se pueden agendar citas en fechas u horas pasadas");
        }
    }

    private void ensureSlotIsFree(Long patientId, Long doctorId, LocalDateTime start) {
        if (appointments.existsScheduledByDoctorAt(doctorId, start)) {
            throw new BusinessConflictException(ErrorCode.DOCTOR_SLOT_TAKEN,
                    "El médico ya tiene una cita programada en la franja que inicia el " + start);
        }
        if (appointments.existsScheduledByPatientAt(patientId, start)) {
            throw new BusinessConflictException(ErrorCode.PATIENT_SLOT_TAKEN,
                    "El paciente ya tiene una cita programada en la franja que inicia el " + start);
        }
    }
}
