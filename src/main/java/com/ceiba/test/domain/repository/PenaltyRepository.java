package com.ceiba.test.domain.repository;

import com.ceiba.test.domain.model.Penalty;
import java.time.LocalDateTime;

public interface PenaltyRepository {

    Penalty save(Penalty penalty);

    long countByPatientSince(Long patientId, LocalDateTime since);
}
