package com.ceiba.test.infrastructure.persistence.mapper;

import com.ceiba.test.domain.model.Patient;
import com.ceiba.test.infrastructure.persistence.entity.PatientEntity;
import org.springframework.stereotype.Component;

@Component
public class PatientPersistenceMapper {

    public PatientEntity toEntity(Patient patient) {
        PatientEntity entity = new PatientEntity();
        entity.setId(patient.getId());
        entity.setFullName(patient.getFullName());
        entity.setDocumentNumber(patient.getDocumentNumber());
        entity.setPhone(patient.getPhone());
        entity.setEmail(patient.getEmail());
        entity.setBirthDate(patient.getBirthDate());
        return entity;
    }

    public Patient toDomain(PatientEntity entity) {
        return Patient.restore(
                entity.getId(),
                entity.getFullName(),
                entity.getDocumentNumber(),
                entity.getPhone(),
                entity.getEmail(),
                entity.getBirthDate()
        );
    }
}
