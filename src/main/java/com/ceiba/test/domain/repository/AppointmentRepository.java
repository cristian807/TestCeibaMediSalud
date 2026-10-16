package com.ceiba.test.domain.repository;

import com.ceiba.test.domain.model.Appointment;
import com.ceiba.test.domain.model.AppointmentFilter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AppointmentRepository {

    Appointment save(Appointment appointment);

    Optional<Appointment> findById(Long id);

    List<Appointment> search(AppointmentFilter filter);

    boolean existsScheduledByDoctorAt(Long doctorId, LocalDateTime startTime);

    boolean existsScheduledByPatientAt(Long patientId, LocalDateTime startTime);

    Set<LocalDateTime> findScheduledStartTimes(Long doctorId, LocalDateTime from, LocalDateTime to);
}
