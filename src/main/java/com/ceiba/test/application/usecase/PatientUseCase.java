package com.ceiba.test.application.usecase;

import com.ceiba.test.application.dto.PatientRequest;
import com.ceiba.test.application.dto.PatientResponse;
import java.util.List;

public interface PatientUseCase {

    PatientResponse register(PatientRequest request);

    PatientResponse getById(Long id);

    List<PatientResponse> list();
}
