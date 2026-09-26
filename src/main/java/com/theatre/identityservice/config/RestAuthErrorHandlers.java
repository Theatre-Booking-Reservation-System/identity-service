package com.theatre.identityservice.config;

import com.theatre.identityservice.util.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthErrorHandlers {

    /** 401 for unauthenticated requests to protected endpoints. */
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return this::writeUnauthorized;
    }

    /** 403 for authenticated callers lacking the required role. */
    public AccessDeniedHandler accessDeniedHandler() {
        return this::writeForbidden;
    }

    private void writeUnauthorized(HttpServletRequest request, HttpServletResponse response,
                                   AuthenticationException ex) throws IOException {
        write(response, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED);
    }

    private void writeForbidden(HttpServletRequest request, HttpServletResponse response,
                                AccessDeniedException ex) throws IOException {
        write(response, HttpStatus.FORBIDDEN, ErrorCode.ACCESS_DENIED);
    }

    private void write(HttpServletResponse response, HttpStatus status, ErrorCode errorCode) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        String body = "{\"statusCode\":\"" + escape(errorCode.getErrorCode())
                + "\",\"statusDescription\":\"" + escape(errorCode.getErrorDescription()) + "\"}";
        response.getWriter().write(body);
    }

    /** Minimal JSON string escaping for the fixed, developer-controlled messages. */
    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
