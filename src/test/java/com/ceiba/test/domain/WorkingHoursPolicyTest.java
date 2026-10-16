package com.ceiba.test.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ceiba.test.domain.exception.ErrorCode;
import com.ceiba.test.domain.exception.InvalidDataException;
import com.ceiba.test.domain.service.WorkingHoursPolicy;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class WorkingHoursPolicyTest {

    private static final LocalDate HOLIDAY = LocalDate.of(2026, 6, 8);
    private final WorkingHoursPolicy policy = new WorkingHoursPolicy(date -> date.equals(HOLIDAY));

    @Test
    void weekdayHasTwentySlotsFrom0800To1800() {
        List<LocalDateTime> slots = policy.slotStarts(LocalDate.of(2026, 6, 2));

        assertThat(slots).hasSize(20);
        assertThat(slots.get(0)).isEqualTo(LocalDateTime.of(2026, 6, 2, 8, 0));
        assertThat(slots.get(19)).isEqualTo(LocalDateTime.of(2026, 6, 2, 17, 30));
    }

    @Test
    void saturdayHasTenSlotsFrom0800To1300() {
        List<LocalDateTime> slots = policy.slotStarts(LocalDate.of(2026, 6, 6));

        assertThat(slots).hasSize(10);
        assertThat(slots.get(9)).isEqualTo(LocalDateTime.of(2026, 6, 6, 12, 30));
    }

    @Test
    void sundaysAndHolidaysHaveNoSlots() {
        assertThat(policy.slotStarts(LocalDate.of(2026, 6, 7))).isEmpty();
        assertThat(policy.slotStarts(HOLIDAY)).isEmpty();
        assertThat(policy.closingTime(HOLIDAY)).isEqualTo(Optional.empty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-06-02T08:00:00", "2026-06-02T17:30:00", "2026-06-06T12:30:00"})
    void acceptsSlotsInsideWorkingHours(String start) {
        policy.validateSlot(LocalDateTime.parse(start));
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-06-02T07:30:00", "2026-06-02T18:00:00", "2026-06-06T13:00:00"})
    void rejectsSlotsOutsideWorkingHours(String start) {
        assertThatThrownBy(() -> policy.validateSlot(LocalDateTime.parse(start)))
                .isInstanceOf(InvalidDataException.class)
                .extracting("code").isEqualTo(ErrorCode.OUTSIDE_WORKING_HOURS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-06-07T09:00:00", "2026-06-08T09:00:00"})
    void rejectsSundaysAndHolidays(String start) {
        assertThatThrownBy(() -> policy.validateSlot(LocalDateTime.parse(start)))
                .isInstanceOf(InvalidDataException.class)
                .extracting("code").isEqualTo(ErrorCode.NON_WORKING_DAY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"2026-06-02T09:15:00", "2026-06-02T09:00:30", "2026-06-02T09:45:00"})
    void rejectsMisalignedSlots(String start) {
        assertThatThrownBy(() -> policy.validateSlot(LocalDateTime.parse(start)))
                .isInstanceOf(InvalidDataException.class)
                .extracting("code").isEqualTo(ErrorCode.INVALID_SLOT_ALIGNMENT);
    }
}
