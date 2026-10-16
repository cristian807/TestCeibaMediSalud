package com.ceiba.test.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record AppointmentRequest(
        @NotNull(message = "El pacienteId es obligatorio")
        Long pacienteId,

        @NotNull(message = "El medicoId es obligatorio")
        Long medicoId,

        @NotNull(message = "La fechaHora es obligatoria")
        @JsonFormat(pattern = ApiDateFormat.DATE_TIME)
        @Schema(type = "string", example = "12-10-2026 6:40", description = "Formato dd-MM-yyyy H:mm")
        LocalDateTime fechaHora
) {
}
