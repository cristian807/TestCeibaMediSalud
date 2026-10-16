package com.ceiba.test.application.dto;

import com.ceiba.test.domain.model.Appointment;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ceiba.test.domain.model.AppointmentStatus;
import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long pacienteId,

        Long medicoId,

        @JsonFormat(pattern = ApiDateFormat.DATE_TIME)
        LocalDateTime fechaHora,

        @JsonFormat(pattern = ApiDateFormat.DATE_TIME)
        LocalDateTime fechaHoraFin,

        AppointmentStatus estado,

        @JsonFormat(pattern = ApiDateFormat.DATE_TIME)
        LocalDateTime fechaCancelacion,

        Long citaOriginalId
) {

    public static AppointmentResponse from(Appointment appointment) {
        return new AppointmentResponse(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getDoctorId(),
                appointment.getStartTime(),
                appointment.endTime(),
                appointment.getStatus(),
                appointment.getCancelledAt(),
                appointment.getRescheduledFromId()
        );
    }
}
