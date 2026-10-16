package com.ceiba.test.infrastructure.persistence.repository;

import com.ceiba.test.infrastructure.persistence.entity.PatientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientJpaRepository extends JpaRepository<PatientEntity, Long> {

    boolean existsByDocumentNumber(String documentNumber);
}
