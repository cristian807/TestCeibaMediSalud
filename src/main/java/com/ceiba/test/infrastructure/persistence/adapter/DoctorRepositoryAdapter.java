package com.ceiba.test.infrastructure.persistence.adapter;

import com.ceiba.test.domain.model.Doctor;
import com.ceiba.test.domain.repository.DoctorRepository;
import com.ceiba.test.infrastructure.persistence.mapper.DoctorPersistenceMapper;
import com.ceiba.test.infrastructure.persistence.repository.DoctorJpaRepository;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class DoctorRepositoryAdapter implements DoctorRepository {

    private final DoctorJpaRepository jpa;
    private final DoctorPersistenceMapper mapper;

    @Override
    public Doctor save(Doctor doctor) {

        return mapper.toDomain(jpa.save(mapper.toEntity(doctor)));
    }

    @Override
    public Optional<Doctor> findById(Long id) {

        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<Doctor> findAll() {

        return jpa.findAll(Sort.by("id")).stream().map(mapper::toDomain).toList();
    }
}
