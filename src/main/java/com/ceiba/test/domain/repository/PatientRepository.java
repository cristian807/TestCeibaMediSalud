package com.ceiba.test.domain.repository;

import com.ceiba.test.domain.model.Patient;
import java.util.List;
import java.util.Optional;

public interface PatientRepository {

    Patient save(Patient patient);

    Optional<Patient> findById(Long id);

    List<Patient> findAll();

    boolean existsByDocumentNumber(String documentNumber);
}
