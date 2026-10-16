package com.ceiba.test.application.dto;

import com.ceiba.test.domain.model.Doctor;

public record DoctorResponse(
        Long id,
        String nombreCompleto,
        String especialidad,
        String telefono,
        String email
) {

    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getFullName(),
                doctor.getSpecialty(),
                doctor.getPhone(),
                doctor.getEmail());
    }
}
