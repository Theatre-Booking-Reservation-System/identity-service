package com.theatre.identityservice.controller;

import com.theatre.identityservice.model.LoginRequest;
import com.theatre.identityservice.model.LoginResponse;
import com.theatre.identityservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login and JWT access-token issuance")
public class AuthController {

    private final AuthService authService;

    @Operation(
            summary = "Authenticate a user and issue a JWT",
            description = "Validates the supplied credentials and, on success, returns a JWT "
                    + "access token together with the authenticated user's profile. This is a "
                    + "public endpoint and does not require authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication succeeded; access token returned"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "423", description = "Account temporarily locked after too many failed attempts")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
