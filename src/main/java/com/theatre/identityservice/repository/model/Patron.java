package com.theatre.identityservice.repository.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "patron")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Patron {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "patron_id", updatable = false, nullable = false)
    private UUID patronId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "contact_no")
    private String contactNo;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "nic_passport_no")
    private String nicPassportNo;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean isVerified = false;

    @Column(name = "loyalty_card_no")
    private String loyaltyCardNo;

    @Column(name = "is_loyalty_holder", nullable = false)
    @Builder.Default
    private boolean isLoyaltyHolder = false;

    @Column(name = "failed_login_count", nullable = false)
    @Builder.Default
    private short failedLoginCount = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "status", nullable = false)
    private Integer status;

    @Column(name = "added_by")
    private String addedBy;

    @Column(name = "added_date")
    private LocalDateTime addedDate;

    @Column(name = "modified_by")
    private String modifiedBy;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;
}
