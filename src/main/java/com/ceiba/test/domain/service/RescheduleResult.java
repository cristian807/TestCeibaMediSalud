package com.ceiba.test.domain.service;

import com.ceiba.test.domain.model.Appointment;

public record RescheduleResult(
        Appointment cancelled,
        Appointment created,
        boolean penaltyApplied
) {
}
