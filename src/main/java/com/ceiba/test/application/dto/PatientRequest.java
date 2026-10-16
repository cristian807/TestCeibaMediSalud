package com.ceiba.test.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record PatientRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre completo debe tener entre 3 y 100 caracteres")
        String nombreCompleto,

        @NotBlank(message = "El documento de identidad es obligatorio")
        @Size(min = 7, max = 20, message = "El documento de identidad debe tener entre 7 y 20 caracteres")
        @Pattern(regexp = "^[A-Za-z0-9\\-]*$",
                message = "El documento de identidad solo puede contener letras, números y guiones")
        String documentoIdentidad,

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(min = 7, max = 12, message = "El numero de telefono debe contener entre 7 y 12 digitos")
        String telefono,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no puede superar los 150 caracteres")
        String email,

        @JsonFormat(pattern = ApiDateFormat.DATE)
        LocalDate fechaNacimiento
) {
}
