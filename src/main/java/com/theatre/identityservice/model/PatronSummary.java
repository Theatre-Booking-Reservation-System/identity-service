package com.theatre.identityservice.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PatronSummary {

    private UUID patronId;
    private String name;
    private String email;
    private String contactNo;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfBirth;

    private String nicPassportNo;
    private boolean verified;
    private String loyaltyCardNo;
    private boolean loyaltyHolder;
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime addedDate;
}
