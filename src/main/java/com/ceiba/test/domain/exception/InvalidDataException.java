package com.ceiba.test.domain.exception;

public class InvalidDataException extends DomainException {

    public InvalidDataException(
            ErrorCode code,
            String message
    ) {
        super(code, message);
    }
}
