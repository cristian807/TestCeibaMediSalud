package com.ceiba.test.domain.exception;

public class BusinessConflictException extends DomainException {

    public BusinessConflictException(
            ErrorCode code,
            String message
    ) {
        super(code, message);
    }
}
