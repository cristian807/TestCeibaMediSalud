package com.ceiba.test.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class Penalty {

    private final Long id;
    private final Long patientId;
    private final Long appointmentId;
    private final LocalDateTime createdAt;

    public static Penalty issue(
            Long patientId,
            Long appointmentId,
            LocalDateTime createdAt
    ) {
        return new Penalty(null, patientId, appointmentId, createdAt);
    }

    public static Penalty restore(
            Long id,
            Long patientId,
            Long appointmentId,
            LocalDateTime createdAt
    ) {
        return new Penalty(id, patientId, appointmentId, createdAt);
    }
}
