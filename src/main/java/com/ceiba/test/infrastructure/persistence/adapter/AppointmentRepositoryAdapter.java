package com.ceiba.test.infrastructure.persistence.adapter;

import com.ceiba.test.domain.exception.BusinessConflictException;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.AppointmentFilter;
import com.ceiba.test.domain.model.AppointmentStatus;
import com.ceiba.test.domain.repository.AppointmentRepository;
import com.ceiba.test.infrastructure.persistence.entity.AppointmentEntity;
import com.ceiba.test.infrastructure.persistence.mapper.AppointmentPersistenceMapper;
import com.ceiba.test.infrastructure.persistence.repository.AppointmentJpaRepository;
import com.ceiba.test.infrastructure.persistence.repository.DoctorJpaRepository;
import com.ceiba.test.infrastructure.persistence.repository.PatientJpaRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class AppointmentRepositoryAdapter implements AppointmentRepository {

    private final AppointmentJpaRepository jpa;
    private final DoctorJpaRepository doctorJpa;
    private final PatientJpaRepository patientJpa;
    private final AppointmentPersistenceMapper mapper;

    @Override
    public Appointment save(Appointment appointment) {
        AppointmentEntity entity = appointment.getId() == null
                ? new AppointmentEntity()
                : jpa.findById(appointment.getId()).orElseThrow();
        mapper.copyToEntity(appointment, entity,
                patientJpa.getReferenceById(appointment.getPatientId()),
                doctorJpa.getReferenceById(appointment.getDoctorId()));
        try {
            return mapper.toDomain(jpa.saveAndFlush(entity));
        } catch (DataIntegrityViolationException ex) {
            throw translate(ex, appointment);
        }
    }

    @Override
    public Optional<Appointment> findById(Long id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Appointment> search(AppointmentFilter filter) {
        Specification<AppointmentEntity> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.doctorId() != null) {
                predicates.add(builder.equal(root.get("doctor").get("id"), filter.doctorId()));
            }
            if (filter.patientId() != null) {
                predicates.add(builder.equal(root.get("patient").get("id"), filter.patientId()));
            }
            if (filter.status() != null) {
                predicates.add(builder.equal(root.get("status"), filter.status()));
            }
            if (filter.from() != null) {
                predicates.add(builder.greaterThanOrEqualTo(root.get("startTime"), filter.from()));
            }
            if (filter.to() != null) {
                predicates.add(builder.lessThan(root.get("startTime"), filter.to()));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };
        return jpa.findAll(specification, Sort.by("startTime", "id")).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsScheduledByDoctorAt(Long doctorId, LocalDateTime startTime) {
        return jpa.existsByDoctorIdAndStartTimeAndStatus(doctorId, startTime, AppointmentStatus.PROGRAMADA);
    }

    @Override
    public boolean existsScheduledByPatientAt(Long patientId, LocalDateTime startTime) {
        return jpa.existsByPatientIdAndStartTimeAndStatus(patientId, startTime, AppointmentStatus.PROGRAMADA);
    }

    @Override
    public Set<LocalDateTime> findScheduledStartTimes(Long doctorId, LocalDateTime from, LocalDateTime to) {
        return new HashSet<>(jpa.findStartTimes(doctorId, AppointmentStatus.PROGRAMADA, from, to));
    }

    private RuntimeException translate(DataIntegrityViolationException ex, Appointment appointment) {
        String cause = String.valueOf(ex.getMostSpecificCause().getMessage()).toUpperCase(Locale.ROOT);
        if (cause.contains("UK_APPOINTMENT_DOCTOR_SLOT")) {
            return new BusinessConflictException(ErrorCode.DOCTOR_SLOT_TAKEN,
                    "El médico ya tiene una cita programada en la franja que inicia el "
                            + appointment.getStartTime());
        }
        if (cause.contains("UK_APPOINTMENT_PATIENT_SLOT")) {
            return new BusinessConflictException(ErrorCode.PATIENT_SLOT_TAKEN,
                    "El paciente ya tiene una cita programada en la franja que inicia el "
                            + appointment.getStartTime());
        }
        return ex;
    }
}
