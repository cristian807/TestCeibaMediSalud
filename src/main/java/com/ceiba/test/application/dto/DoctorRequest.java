package com.ceiba.test.application.dto;

import jakarta.validation.constraints.*;

public record DoctorRequest(
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(min = 3, max = 100, message = "El nombre completo debe tener entre 3 y 100 caracteres")
        String nombreCompleto,

        @NotBlank(message = "La especialidad es obligatoria")
        @Size(max = 100, message = "La especialidad no puede superar los 100 caracteres")
        String especialidad,

        @Size(min = 7, max = 12, message = "El numero de telefono debe contener entre 7 y 12 digitos")
        String telefono,

        @Email(message = "El email no tiene un formato válido")
        @Size(max = 150, message = "El email no puede superar los 150 caracteres")
        String email) {
}
