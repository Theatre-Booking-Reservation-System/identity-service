package com.theatre.identityservice.controller;

import com.theatre.identityservice.model.PatronRegisterRequest;
import com.theatre.identityservice.model.PatronRegisterResponse;
import com.theatre.identityservice.service.PatronService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patron")
@RequiredArgsConstructor
public class PatronController {

    private final PatronService patronService;

    @PostMapping("/register")
    public ResponseEntity<PatronRegisterResponse> register(@Valid @RequestBody PatronRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(patronService.register(request));
    }
}
