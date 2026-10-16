package com.ceiba.test.domain.exception;

public class ResourceNotFoundException extends DomainException {

    public ResourceNotFoundException(
            ErrorCode code,
            String message
    ) {
        super(code, message);
    }
}
