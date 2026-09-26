package com.theatre.identityservice.util;

import lombok.Getter;

@Getter
public enum ErrorCode {

    DEFAULT("ERR_00", "Internal Server Error"),
    VALIDATION_FAILED("ERR_01", "Request validation failed"),
    INVALID_USER_ID_OR_PASSWORD("ATH_01", "Invalid User ID or Password"),
    EMAIL_ALREADY_REGISTERED("ATH_02", "Email is already registered"),
    ACCOUNT_LOCKED("ATH_03", "Account is locked due to too many failed login attempts"),
    UNAUTHORIZED("ATH_04", "Authentication required"),
    ACCESS_DENIED("ATH_05", "You do not have permission to perform this action"),
    PATRON_NOT_FOUND("PAT_01", "Patron not found");

    final String errorCode;
    final String errorDescription;

    ErrorCode(String errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

}
