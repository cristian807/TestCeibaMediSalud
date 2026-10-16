package com.ceiba.test.application.dto;

import com.ceiba.test.domain.service.RescheduleResult;

public record RescheduleResponse(
        AppointmentResponse citaCancelada,
        AppointmentResponse citaNueva,
        boolean penalizacionAplicada
) {

    public static RescheduleResponse from(RescheduleResult result) {
        return new RescheduleResponse(
                AppointmentResponse.from(result.cancelled()),
                AppointmentResponse.from(result.created()), result.penaltyApplied()
        );
    }
}
