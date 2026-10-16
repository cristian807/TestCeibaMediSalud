package com.ceiba.test.application.dto;

import com.ceiba.test.domain.model.AppointmentStatus;
import java.time.LocalDate;

public record AppointmentSearchCriteria(
        Long medicoId,
        Long pacienteId,
        AppointmentStatus estado,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {
}
