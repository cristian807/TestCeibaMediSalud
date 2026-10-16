package com.ceiba.test.domain.service;

import com.ceiba.test.domain.model.Appointment;

public record CancellationResult(
        Appointment appointment,
        boolean penaltyApplied
) {
}
