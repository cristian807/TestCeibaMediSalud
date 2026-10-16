package com.ceiba.test.application.service;

import com.ceiba.test.application.dto.DoctorRequest;
import com.ceiba.test.application.dto.DoctorResponse;
import com.ceiba.test.application.usecase.DoctorUseCase;
import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.ResourceNotFoundException;
import com.ceiba.test.domain.model.Doctor;
import com.ceiba.test.domain.repository.DoctorRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorService implements DoctorUseCase {

    private final DoctorRepository doctors;

    public DoctorService(DoctorRepository doctors) {
        this.doctors = doctors;
    }

    @Override
    public DoctorResponse register(DoctorRequest request) {
        Doctor doctor = Doctor.register(
                request.nombreCompleto(),
                request.especialidad(),
                request.telefono(),
                request.email()
        );
        return DoctorResponse.from(doctors.save(doctor));
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getById(Long id) {
        return doctors.findById(id)
                .map(DoctorResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.DOCTOR_NOT_FOUND,
                        "No existe un médico con id " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> list() {
        return doctors.findAll().stream().map(DoctorResponse::from).toList();
    }
}
