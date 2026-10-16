package com.ceiba.test.application.dto;

import com.ceiba.test.domain.service.CancellationResult;

public record CancellationResponse(
        AppointmentResponse cita,
        boolean penalizacionAplicada
) {

    public static CancellationResponse from(CancellationResult result) {
        return new CancellationResponse(
                AppointmentResponse.from(result.appointment()),
                result.penaltyApplied()
        );
    }
}
