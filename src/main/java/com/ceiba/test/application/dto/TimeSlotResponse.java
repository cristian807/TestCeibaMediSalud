package com.ceiba.test.application.dto;

import com.ceiba.test.domain.model.TimeSlot;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public record TimeSlotResponse(
        @JsonFormat(pattern = ApiDateFormat.DATE_TIME)
        LocalDateTime inicio,

        @JsonFormat(pattern = ApiDateFormat.DATE_TIME)
        LocalDateTime fin
) {

    public static TimeSlotResponse from(TimeSlot slot) {

        return new TimeSlotResponse(slot.start(), slot.end());
    }
}
