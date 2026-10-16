package com.ceiba.test.infrastructure.persistence.repository;

import com.ceiba.test.domain.model.AppointmentStatus;
import com.ceiba.test.infrastructure.persistence.entity.AppointmentEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppointmentJpaRepository extends JpaRepository<AppointmentEntity, Long>, JpaSpecificationExecutor<AppointmentEntity> {

    boolean existsByDoctorIdAndStartTimeAndStatus(
            Long doctorId,
            LocalDateTime startTime,
            AppointmentStatus status
    );

    boolean existsByPatientIdAndStartTimeAndStatus(
            Long patientId,
            LocalDateTime startTime,
            AppointmentStatus status
    );

    @Query("""
            select a.startTime 
            from AppointmentEntity a
            where a.doctor.id = :doctorId 
            and a.status = :status
            and a.startTime >= :from and a.startTime < :to
            """)
    List<LocalDateTime> findStartTimes(
            @Param("doctorId") Long doctorId,
            @Param("status") AppointmentStatus status,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
