package com.ceiba.test.infrastructure.persistence.adapter;

import com.ceiba.test.domain.model.Penalty;
import com.ceiba.test.domain.repository.PenaltyRepository;
import com.ceiba.test.infrastructure.persistence.mapper.PenaltyPersistenceMapper;
import com.ceiba.test.infrastructure.persistence.repository.PenaltyJpaRepository;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class PenaltyRepositoryAdapter implements PenaltyRepository {

    private final PenaltyJpaRepository jpa;
    private final PenaltyPersistenceMapper mapper;


    @Override
    public Penalty save(Penalty penalty) {

        return mapper.toDomain(jpa.save(mapper.toEntity(penalty)));
    }

    @Override
    public long countByPatientSince(Long patientId, LocalDateTime since) {
        return jpa.countByPatientIdAndCreatedAtGreaterThanEqual(patientId, since);
    }
}
