package com.ceiba.test.infrastructure.persistence.repository;

import com.ceiba.test.infrastructure.persistence.entity.DoctorEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorJpaRepository extends JpaRepository<DoctorEntity, Long> {
}
