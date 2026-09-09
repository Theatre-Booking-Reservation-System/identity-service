package com.theatre.identityservice.exception;

import com.theatre.identityservice.util.ErrorCode;

import java.util.Optional;

public class ServiceException extends RuntimeException {

    private final String errorCode;
    private final String errorDescription;

    public ServiceException(String errorCode, String errorDescription) {
        super(errorDescription);
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getErrorDescription());
        this.errorCode = errorCode.getErrorCode();
        this.errorDescription = errorCode.getErrorDescription();
    }

    public Optional<String> getErrorCode() {
        return Optional.ofNullable(this.errorCode);
    }

    public Optional<String> getErrorDescription() {
        return Optional.ofNullable(this.errorDescription);
    }

}
