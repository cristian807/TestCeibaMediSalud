package com.ceiba.test.domain.model;

import java.time.LocalDateTime;

public record TimeSlot(
        LocalDateTime start,
        LocalDateTime end) {
}
