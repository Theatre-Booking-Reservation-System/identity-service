package com.theatre.identityservice.exception;

import com.theatre.identityservice.model.CommonResponse;
import com.theatre.identityservice.util.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<CommonResponse> handleServiceException(ServiceException ex) {
        String errorCode = ex.getErrorCode().orElse(ErrorCode.DEFAULT.getErrorCode());
        String description = ex.getErrorDescription().orElse(ErrorCode.DEFAULT.getErrorDescription());
        return build(httpStatusFor(errorCode), errorCode, description);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<CommonResponse> handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(GlobalExceptionHandler::formatFieldError)
                .collect(Collectors.joining("; "));
        String description = details.isBlank() ? "Request validation failed" : details;
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.getErrorCode(), description);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ResponseEntity<CommonResponse> handleMalformedRequest(Exception ex) {
        return build(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_FAILED.getErrorCode(),
                "Malformed or invalid request");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<CommonResponse> handleBadCredentials(BadCredentialsException ex) {
        return build(HttpStatus.UNAUTHORIZED,
                ErrorCode.INVALID_USER_ID_OR_PASSWORD.getErrorCode(),
                ErrorCode.INVALID_USER_ID_OR_PASSWORD.getErrorDescription());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<CommonResponse> handleAuthentication(AuthenticationException ex) {
        return build(HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED.getErrorCode(),
                ErrorCode.UNAUTHORIZED.getErrorDescription());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<CommonResponse> handleAccessDenied(AccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED.getErrorCode(),
                ErrorCode.ACCESS_DENIED.getErrorDescription());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponse> handleUnexpected(Exception ex) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.DEFAULT.getErrorCode(),
                ErrorCode.DEFAULT.getErrorDescription());
    }

    private static ResponseEntity<CommonResponse> build(HttpStatus status, String code, String description) {
        return ResponseEntity.status(status).body(CommonResponse.builder()
                .statusCode(code)
                .statusDescription(description)
                .build());
    }

    private static String formatFieldError(FieldError fe) {
        return fe.getField() + ": " + fe.getDefaultMessage();
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
        if (ErrorCode.ACCOUNT_LOCKED.getErrorCode().equals(errorCode)) {
            return HttpStatus.LOCKED;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}
