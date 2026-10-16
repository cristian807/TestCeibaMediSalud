package com.ceiba.test.domain.model;

import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.InvalidDataException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Locale;

@AllArgsConstructor
@Getter
public class Patient {

    private final Long id;
    private final String fullName;
    private final String documentNumber;
    private final String phone;
    private final String email;
    private final LocalDate birthDate;

    public static Patient register(
            String fullName,
            String documentNumber,
            String phone,
            String email,
            LocalDate birthDate
    ) {
        if (fullName == null || fullName.isBlank()) {
            throw new InvalidDataException(ErrorCode.VALIDATION_ERROR, "El nombre del paciente es obligatorio");
        }
        if (documentNumber == null || documentNumber.isBlank()) {
            throw new InvalidDataException(ErrorCode.VALIDATION_ERROR, "El documento de identidad es obligatorio");
        }
        return new Patient(
                null,
                fullName.trim(),
                normalizeDocument(documentNumber), phone, email, birthDate
        );
    }

    public static Patient restore(
            Long id,
            String fullName,
            String documentNumber,
            String phone,
            String email,
            LocalDate birthDate
    ) {
        return new Patient(id, fullName, documentNumber, phone, email, birthDate);
    }

    public boolean hasValidBirthDate(LocalDate today) {

        return birthDate == null || !birthDate.isAfter(today);
    }

    public static String normalizeDocument(String documentNumber) {
        return documentNumber.trim().toUpperCase(Locale.ROOT);
    }

}
