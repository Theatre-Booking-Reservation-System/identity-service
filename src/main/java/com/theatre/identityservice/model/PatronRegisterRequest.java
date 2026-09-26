package com.theatre.identityservice.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PatronRegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid address")
    private String email;

    // Optional contact number. Allows digits, spaces, +, -, and parentheses.
    @Pattern(regexp = "^[0-9+()\\-\\s]{7,20}$", message = "Contact number must be a valid phone number")
    private String contactNo;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
