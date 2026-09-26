package com.theatre.identityservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

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

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    // Sri Lankan NIC (old 9 digits + V/X, or new 12 digits) or passport number.
    @NotBlank(message = "NIC or passport number is required")
    @Pattern(
            regexp = "^([0-9]{9}[vVxX]|[0-9]{12}|[A-Za-z0-9]{6,15})$",
            message = "NIC or passport number must be valid")
    private String nicPassportNo;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
