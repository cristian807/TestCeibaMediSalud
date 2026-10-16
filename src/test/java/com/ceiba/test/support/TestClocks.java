package com.ceiba.test.support;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public final class TestClocks {

    public static final ZoneId ZONE = ZoneId.of("America/Bogota");

    public static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 1, 10, 0);

    private TestClocks() {
    }

    public static Clock fixedAt(LocalDateTime dateTime) {
        return Clock.fixed(dateTime.atZone(ZONE).toInstant(), ZONE);
    }

    public static Clock fixed() {
        return fixedAt(NOW);
    }

    public static Instant instant() {
        return NOW.atZone(ZONE).toInstant();
    }
}
