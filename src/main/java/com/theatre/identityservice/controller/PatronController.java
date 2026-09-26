package com.theatre.identityservice.controller;

import com.theatre.identityservice.model.CommonResponse;
import com.theatre.identityservice.model.PatronDetailResponse;
import com.theatre.identityservice.model.PatronListResponse;
import com.theatre.identityservice.model.PatronRegisterRequest;
import com.theatre.identityservice.model.PatronRegisterResponse;
import com.theatre.identityservice.service.PatronService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/patron")
@RequiredArgsConstructor
@Tag(name = "Patrons", description = "Self-service patron account registration")
public class PatronController {

    private final PatronService patronService;

    @Operation(
            summary = "Register a new patron account",
            description = "Creates a new patron with the supplied name, email and password. "
                    + "This is a public endpoint and does not require authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Patron account created"),
            @ApiResponse(responseCode = "400", description = "Request body failed validation"),
            @ApiResponse(responseCode = "409", description = "A patron with the given email already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<PatronRegisterResponse> register(@Valid @RequestBody PatronRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patronService.register(request));
    }

    @Operation(
            summary = "List all registered patrons",
            description = "Returns every patron account as a read-only summary, most recently "
                    + "registered first. Intended for the admin portal; requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patron list returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication"),
            @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<PatronListResponse> listPatrons() {
        return ResponseEntity.ok(patronService.listAllPatrons());
    }

    @Operation(
            summary = "Get a single patron's details",
            description = "Returns the details of one patron by id. Intended for the admin "
                    + "portal; requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patron found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication"),
            @ApiResponse(responseCode = "403", description = "Caller is neither an admin nor a patron"),
            @ApiResponse(responseCode = "404", description = "No patron exists for the given id")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'PATRON')")
    @GetMapping("/{patronId}")
    public ResponseEntity<PatronDetailResponse> getPatron(@PathVariable UUID patronId) {
        return ResponseEntity.ok(patronService.getPatron(patronId));
    }

    @Operation(
            summary = "Search patrons by filters",
            description = "Returns patrons matching any combination of the optional filters "
                    + "(name, email, loyaltyHolder, status). Name and email are matched "
                    + "case-insensitively as a partial 'contains'. Omitting all filters returns "
                    + "every patron. Intended for the admin portal; requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Matching patrons returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication"),
            @ApiResponse(responseCode = "403", description = "Caller is not an admin")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<PatronListResponse> searchPatrons(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) Boolean loyaltyHolder,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(patronService.searchPatrons(name, email, loyaltyHolder, status));
    }

    @Operation(
            summary = "Unlock a locked patron account",
            description = "Clears any active lock and resets the failed-login counter for the "
                    + "given patron, letting them log in again immediately. Idempotent. "
                    + "Restricted to admins.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Patron account unlocked"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid authentication"),
            @ApiResponse(responseCode = "403", description = "Caller is not an admin"),
            @ApiResponse(responseCode = "404", description = "No patron exists for the given id")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{patronId}/unlock")
    public ResponseEntity<CommonResponse> unlockPatron(@PathVariable UUID patronId,
                                                       Authentication authentication) {
        String performedBy = authentication != null ? authentication.getName() : null;
        return ResponseEntity.ok(patronService.unlockPatron(patronId, performedBy));
    }
}
