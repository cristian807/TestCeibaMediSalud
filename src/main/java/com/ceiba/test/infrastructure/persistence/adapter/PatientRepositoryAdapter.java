package com.ceiba.test.infrastructure.persistence.adapter;

import com.ceiba.test.domain.exception.BusinessConflictException;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.model.Patient;
import com.ceiba.test.domain.repository.PatientRepository;
import com.ceiba.test.infrastructure.persistence.mapper.PatientPersistenceMapper;
import com.ceiba.test.infrastructure.persistence.repository.PatientJpaRepository;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class PatientRepositoryAdapter implements PatientRepository {

    private final PatientJpaRepository jpa;
    private final PatientPersistenceMapper mapper;

    @Override
    public Patient save(Patient patient) {
        try {
            return mapper.toDomain(jpa.saveAndFlush(mapper.toEntity(patient)));
        } catch (DataIntegrityViolationException ex) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_DOCUMENT,
                    "Ya existe un paciente registrado con el documento " + patient.getDocumentNumber());
        }
    }

    @Override
    public Optional<Patient> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Patient> findAll() {
        return jpa.findAll(Sort.by("id")).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsByDocumentNumber(String documentNumber) {
        return jpa.existsByDocumentNumber(documentNumber);
    }
}
