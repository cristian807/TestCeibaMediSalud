package com.ceiba.test.infrastructure.persistence.mapper;

import com.ceiba.test.domain.model.Doctor;
import com.ceiba.test.infrastructure.persistence.entity.DoctorEntity;
import org.springframework.stereotype.Component;

@Component
public class DoctorPersistenceMapper {

    public DoctorEntity toEntity(Doctor doctor) {
        DoctorEntity entity = new DoctorEntity();
        entity.setId(doctor.getId());
        entity.setFullName(doctor.getFullName());
        entity.setSpecialty(doctor.getSpecialty());
        entity.setPhone(doctor.getPhone());
        entity.setEmail(doctor.getEmail());
        return entity;
    }

    public Doctor toDomain(DoctorEntity entity) {
        return Doctor.restore(
                entity.getId(),
                entity.getFullName(),
                entity.getSpecialty(),
                entity.getPhone(),
                entity.getEmail()
        );
    }
}
