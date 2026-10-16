package com.ceiba.test.domain.model;

import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.InvalidDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Doctor {

    private final Long id;
    private final String fullName;
    private final String specialty;
    private final String phone;
    private final String email;

    public static Doctor register(
            String fullName,
            String specialty,
            String phone,
            String email
    ) {
        if (fullName == null || fullName.isBlank()) {
            throw new InvalidDataException(ErrorCode.VALIDATION_ERROR, "El nombre del médico es obligatorio");
        }
        if (specialty == null || specialty.isBlank()) {
            throw new InvalidDataException(ErrorCode.VALIDATION_ERROR, "La especialidad es obligatoria");
        }
        return new Doctor(null, fullName.trim(), specialty.trim(), phone, email);
    }

    public static Doctor restore(
            Long id,
            String fullName,
            String specialty,
            String phone,
            String email
    ) {
        return new Doctor(id, fullName, specialty, phone, email);
    }
}
