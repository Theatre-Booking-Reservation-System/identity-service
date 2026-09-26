package com.theatre.identityservice.exception;

import com.theatre.identityservice.model.CommonResponse;
import com.theatre.identityservice.util.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<CommonResponse> handleServiceException(ServiceException ex) {
        String errorCode = ex.getErrorCode().orElse(ErrorCode.DEFAULT.getErrorCode());
        CommonResponse commonResponse = CommonResponse.builder()
                .statusCode(errorCode)
                .statusDescription(ex.getErrorDescription().orElse(ErrorCode.DEFAULT.getErrorDescription()))
                .build();
        return ResponseEntity.status(httpStatusFor(errorCode)).body(commonResponse);
    }

    private HttpStatus httpStatusFor(String errorCode) {
        if (ErrorCode.PATRON_NOT_FOUND.getErrorCode().equals(errorCode)) {
            return HttpStatus.NOT_FOUND;
        }
        if (ErrorCode.EMAIL_ALREADY_REGISTERED.getErrorCode().equals(errorCode)) {
            return HttpStatus.CONFLICT;
        }
        if (ErrorCode.INVALID_USER_ID_OR_PASSWORD.getErrorCode().equals(errorCode)) {
            return HttpStatus.UNAUTHORIZED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
