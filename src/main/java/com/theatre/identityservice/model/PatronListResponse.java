package com.theatre.identityservice.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class PatronListResponse extends CommonResponse {

    private long totalCount;
    private List<PatronSummary> patrons;
}
