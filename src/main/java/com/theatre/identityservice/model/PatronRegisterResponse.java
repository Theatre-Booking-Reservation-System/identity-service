package com.theatre.identityservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class PatronRegisterResponse extends CommonResponse {

    private UUID patronId;
    private String name;
    private String email;
}
