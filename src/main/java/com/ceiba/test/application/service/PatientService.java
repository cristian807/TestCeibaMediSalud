package com.ceiba.test.application.service;

import com.ceiba.test.application.dto.PatientRequest;
import com.ceiba.test.application.dto.PatientResponse;
import com.ceiba.test.application.usecase.PatientUseCase;
import com.ceiba.test.domain.exception.BusinessConflictException;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.ResourceNotFoundException;
import com.ceiba.test.domain.model.Patient;
import com.ceiba.test.domain.repository.PatientRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientService implements PatientUseCase {

    private final PatientRepository patients;

    public PatientService(PatientRepository patients) {
        this.patients = patients;
    }

    @Override
    public PatientResponse register(PatientRequest request) {
        Patient patient = Patient.register(
                request.nombreCompleto(),
                request.documentoIdentidad(),
                request.telefono(), request.email(),
                request.fechaNacimiento()
        );
        if (patients.existsByDocumentNumber(patient.getDocumentNumber())) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_DOCUMENT,
                    "Ya existe un paciente registrado con el documento " + patient.getDocumentNumber());
        }
        return PatientResponse.from(patients.save(patient));
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getById(Long id) {
        return patients.findById(id)
                .map(PatientResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCode.PATIENT_NOT_FOUND,
                        "No existe un paciente con id " + id)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PatientResponse> list() {
        return patients.findAll()
                .stream()
                .map(PatientResponse::from)
                .toList();
    }
}
