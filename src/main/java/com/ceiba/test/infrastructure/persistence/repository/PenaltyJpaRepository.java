package com.ceiba.test.infrastructure.persistence.repository;

import com.ceiba.test.infrastructure.persistence.entity.PenaltyEntity;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PenaltyJpaRepository extends JpaRepository<PenaltyEntity, Long> {

    long countByPatientIdAndCreatedAtGreaterThanEqual(Long patientId, LocalDateTime since);
}
