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
    public ResponseEntity<CommonResponse> handleAuthException(ServiceException ex) {
        CommonResponse commonResponse = CommonResponse.builder()
                .statusCode(ex.getErrorCode().orElse(ErrorCode.DEFAULT.getErrorCode()))
                .statusDescription(ex.getErrorDescription().orElse(ErrorCode.DEFAULT.getErrorDescription()))
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(commonResponse);
    }
}
