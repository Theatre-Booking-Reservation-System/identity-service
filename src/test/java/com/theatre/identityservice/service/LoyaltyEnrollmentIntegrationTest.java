package com.theatre.identityservice.service;

import com.theatre.identityservice.exception.ServiceException;
import com.theatre.identityservice.model.LoyaltyEnrollResponse;
import com.theatre.identityservice.repository.PatronRepository;
import com.theatre.identityservice.repository.model.Patron;
import com.theatre.identityservice.util.ErrorCode;
import com.theatre.identityservice.util.PatronStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Loyalty enrolment behaviour against a real (in-memory) database. */
@SpringBootTest
class LoyaltyEnrollmentIntegrationTest {

    @Autowired
    private PatronService patronService;

    @Autowired
    private PatronRepository patronRepository;

    private static final String EMAIL = "loyalty.test@example.com";

    private UUID patronId;

    @BeforeEach
    void setUp() {
        patronRepository.findByEmail(EMAIL).ifPresent(patronRepository::delete);
        Patron patron = patronRepository.save(Patron.builder()
                .name("Loyalty Test")
                .email(EMAIL)
                .passwordHash("x")
                .isVerified(true)
                .status(PatronStatus.ACTIVE.getCode())
                .addedBy("test")
                .addedDate(LocalDateTime.now())
                .build());
        patronId = patron.getPatronId();
    }

    @Test
    void enrollGeneratesCardAndUpdatesFields() {
        LoyaltyEnrollResponse response = patronService.enrollLoyalty(patronId, "PATRON:" + EMAIL);

        assertThat(response.getStatusCode()).isEqualTo("SUCCESS");
        assertThat(response.getStatusDescription()).isNotBlank();
        assertThat(response.getPatronId()).isEqualTo(patronId);
        assertThat(response.getLoyaltyCardNo()).startsWith("LOY-");

        Patron saved = patronRepository.findById(patronId).orElseThrow();
        assertThat(saved.isLoyaltyHolder()).isTrue();
        assertThat(saved.getLoyaltyCardNo()).isEqualTo(response.getLoyaltyCardNo());
        assertThat(saved.getModifiedBy()).isEqualTo("PATRON:" + EMAIL);
    }

    @Test
    void enrollTwiceIsRejected() {
        patronService.enrollLoyalty(patronId, "PATRON:" + EMAIL);

        assertThatThrownBy(() -> patronService.enrollLoyalty(patronId, "PATRON:" + EMAIL))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getErrorCode())
                        .contains(ErrorCode.ALREADY_LOYALTY_MEMBER.getErrorCode()));
    }

    @Test
    void enrollUnknownPatronIsNotFound() {
        assertThatThrownBy(() ->
                patronService.enrollLoyalty(UUID.randomUUID(), "ADMIN:admin@test.com"))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getErrorCode())
                        .contains(ErrorCode.PATRON_NOT_FOUND.getErrorCode()));
    }
}
