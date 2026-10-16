package com.ceiba.test.application.usecase;

import com.ceiba.test.application.dto.AppointmentRequest;
import com.ceiba.test.application.dto.AppointmentResponse;
import com.ceiba.test.application.dto.AppointmentSearchCriteria;
import com.ceiba.test.application.dto.CancellationResponse;
import com.ceiba.test.application.dto.RescheduleRequest;
import com.ceiba.test.application.dto.RescheduleResponse;
import com.ceiba.test.application.dto.TimeSlotResponse;
import java.time.LocalDate;
import java.util.List;

public interface AppointmentUseCase {

    AppointmentResponse create(AppointmentRequest request);

    CancellationResponse cancel(Long appointmentId);

    RescheduleResponse reschedule(Long appointmentId, RescheduleRequest request);

    AppointmentResponse getById(Long appointmentId);

    List<AppointmentResponse> search(AppointmentSearchCriteria criteria);

    List<TimeSlotResponse> findAvailableSlots(Long doctorId, LocalDate fromDate, LocalDate toDate);
}
