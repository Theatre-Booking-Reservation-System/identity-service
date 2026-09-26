package com.theatre.identityservice.util;

import lombok.Getter;

@Getter
public enum ErrorCode {

    DEFAULT("ERR_00", "Internal Server Error"),
    INVALID_USER_ID_OR_PASSWORD("ATH_01", "Invalid User ID or Password"),
    EMAIL_ALREADY_REGISTERED("ATH_02", "Email is already registered"),
    PATRON_NOT_FOUND("PAT_01", "Patron not found");

    final String errorCode;
    final String errorDescription;

    ErrorCode(String errorCode, String errorDescription) {
        this.errorCode = errorCode;
        this.errorDescription = errorDescription;
    }

}
