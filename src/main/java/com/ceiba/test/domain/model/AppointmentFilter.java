package com.ceiba.test.domain.model;

import java.time.LocalDateTime;

public record AppointmentFilter(
        Long doctorId,
        Long patientId,
        AppointmentStatus status,
        LocalDateTime from,
        LocalDateTime to
) {
}
