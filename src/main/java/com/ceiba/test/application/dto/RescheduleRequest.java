package com.ceiba.test.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record RescheduleRequest(
        @NotNull(message = "La nuevaFechaHora es obligatoria")
        @JsonFormat(pattern = ApiDateFormat.DATE_TIME)
        @Schema(type = "string", example = "12-10-2026 6:40", description = "Formato dd-MM-yyyy H:mm")
        LocalDateTime nuevaFechaHora) {
}
