package com.ceiba.test.application.dto;

import com.ceiba.test.domain.model.Patient;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record PatientResponse(
        Long id,
        String nombreCompleto,
        String documentoIdentidad,
        String telefono,
        String email,
        @JsonFormat(pattern = ApiDateFormat.DATE)
        LocalDate fechaNacimiento
) {

    public static PatientResponse from(Patient patient) {
        return new PatientResponse(
                patient.getId(),
                patient.getFullName(),
                patient.getDocumentNumber(),
                patient.getPhone(),
                patient.getEmail(),
                patient.getBirthDate()
        );
    }
}
