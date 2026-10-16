package com.ceiba.test.presentation.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(OffsetDateTime timestamp, int status, String error, String code, String message,
                            String path, List<FieldViolation> details) {

    public record FieldViolation(String field, String message) {
    }
}
