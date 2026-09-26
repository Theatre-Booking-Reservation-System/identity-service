package com.theatre.identityservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class PatronDetailResponse extends CommonResponse {

    private PatronSummary patron;
}
