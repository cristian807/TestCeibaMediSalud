package com.ceiba.test.application.usecase;

import com.ceiba.test.application.dto.DoctorRequest;
import com.ceiba.test.application.dto.DoctorResponse;
import java.util.List;

public interface DoctorUseCase {

    DoctorResponse register(DoctorRequest request);

    DoctorResponse getById(Long id);

    List<DoctorResponse> list();
}
