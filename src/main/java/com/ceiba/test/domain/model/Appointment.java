package com.ceiba.test.domain.model;

import com.ceiba.test.domain.exception.BusinessConflictException;
import com.ceiba.test.domain.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Appointment {

    public static final Duration SLOT_DURATION = Duration.ofMinutes(30);
    public static final Duration LATE_CANCELLATION_THRESHOLD = Duration.ofHours(2);

    private final Long id;
    private final Long patientId;
    private final Long doctorId;
    private final LocalDateTime startTime;
    private AppointmentStatus status;
    private LocalDateTime cancelledAt;
    private final LocalDateTime createdAt;
    private final Long rescheduledFromId;

    public static Appointment schedule(
            Long patientId,
            Long doctorId,
            LocalDateTime startTime,
            LocalDateTime createdAt,
            Long rescheduledFromId
    ) {
        return new Appointment(
                null,
                patientId,
                doctorId,
                startTime,
                AppointmentStatus.PROGRAMADA,
                null,
                createdAt,
                rescheduledFromId
        );
    }

    public static Appointment restore(
            Long id,
            Long patientId,
            Long doctorId,
            LocalDateTime startTime,
            AppointmentStatus status,
            LocalDateTime cancelledAt,
            LocalDateTime createdAt,
            Long rescheduledFromId
    ) {
        return new Appointment(
                id,
                patientId,
                doctorId,
                startTime,
                status,
                cancelledAt,
                createdAt,
                rescheduledFromId
        );
    }

    public LocalDateTime endTime() {
        return startTime.plus(SLOT_DURATION);
    }

    public boolean isScheduled() {

        return status == AppointmentStatus.PROGRAMADA;
    }

    public boolean isLateCancellation(LocalDateTime now) {
        return now.isAfter(startTime.minus(LATE_CANCELLATION_THRESHOLD));
    }

    public void cancel(LocalDateTime now) {
        if (!isScheduled()) {
            throw new BusinessConflictException(ErrorCode.APPOINTMENT_NOT_CANCELLABLE,
                    "Solo se pueden cancelar citas en estado PROGRAMADA; la cita %d está %s"
                            .formatted(id, status));
        }
        this.status = AppointmentStatus.CANCELADA;
        this.cancelledAt = now;
    }
}
