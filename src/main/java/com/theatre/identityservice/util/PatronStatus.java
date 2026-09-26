package com.theatre.identityservice.util;

import lombok.Getter;

@Getter
public enum PatronStatus {

    ACTIVE(1),
    LOCKED(9);

    private final int code;

    PatronStatus(int code) {
        this.code = code;
    }

    public static PatronStatus fromCode(Integer code) {
        if (code != null) {
            for (PatronStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
        }
        throw new IllegalArgumentException("Unknown patron status code: " + code);
    }
}
