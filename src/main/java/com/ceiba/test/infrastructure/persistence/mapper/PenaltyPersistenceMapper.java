package com.ceiba.test.infrastructure.persistence.mapper;

import com.ceiba.test.domain.model.Penalty;
import com.ceiba.test.infrastructure.persistence.entity.PenaltyEntity;
import org.springframework.stereotype.Component;

@Component
public class PenaltyPersistenceMapper {

    public PenaltyEntity toEntity(Penalty penalty) {
        PenaltyEntity entity = new PenaltyEntity();
        entity.setId(penalty.getId());
        entity.setPatientId(penalty.getPatientId());
        entity.setAppointmentId(penalty.getAppointmentId());
        entity.setCreatedAt(penalty.getCreatedAt());
        return entity;
    }

    public Penalty toDomain(PenaltyEntity entity) {
        return Penalty.restore(
                entity.getId(),
                entity.getPatientId(),
                entity.getAppointmentId(),
                entity.getCreatedAt()
        );
    }
}
