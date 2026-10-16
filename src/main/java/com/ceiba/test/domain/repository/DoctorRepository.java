package com.ceiba.test.domain.repository;

import com.ceiba.test.domain.model.Doctor;
import java.util.List;
import java.util.Optional;

public interface DoctorRepository {

    Doctor save(Doctor doctor);

    Optional<Doctor> findById(Long id);

    List<Doctor> findAll();
}
