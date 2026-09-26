package com.theatre.identityservice.service;

import com.theatre.identityservice.exception.ServiceException;
import com.theatre.identityservice.model.LoginRequest;
import com.theatre.identityservice.repository.PatronRepository;
import com.theatre.identityservice.repository.model.Patron;
import com.theatre.identityservice.util.ErrorCode;
import com.theatre.identityservice.util.PatronStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * End-to-end lockout behaviour against a real (in-memory) database, proving:
 *  - the failed-login count is actually persisted (survives the login rollback),
 *  - status flips ACTIVE(1) -> LOCKED(9) at the configured threshold,
 *  - unlock restores status, count and lock timestamp.
 */
@SpringBootTest
@TestPropertySource(properties = {
        "auth.max-failed-attempts=3",
        "auth.lockout-duration-minutes=15"
})
class PatronLockoutIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private PatronService patronService;

    @Autowired
    private PatronRepository patronRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String EMAIL = "lockout.test@example.com";
    private static final String CORRECT_PASSWORD = "Correct@1234";

    private UUID patronId;

    @BeforeEach
    void setUp() {
        patronRepository.findByEmail(EMAIL).ifPresent(patronRepository::delete);

        Patron patron = patronRepository.save(Patron.builder()
                .name("Lockout Test")
                .email(EMAIL)
                .passwordHash(passwordEncoder.encode(CORRECT_PASSWORD))
                .isVerified(true)
                .status(PatronStatus.ACTIVE.getCode())
                .addedBy("test")
                .addedDate(LocalDateTime.now())
                .build());
        patronId = patron.getPatronId();
    }

    private void attemptWrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail(EMAIL);
        request.setPassword("Wrong@0000");
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void failedCountIsPersistedAndIncrements() {
        attemptWrongPassword();
        assertThat(reload().getFailedLoginCount()).isEqualTo((short) 1);

        attemptWrongPassword();
        assertThat(reload().getFailedLoginCount()).isEqualTo((short) 2);
    }

    @Test
    void accountLocksAtThresholdWithStatusNine() {
        attemptWrongPassword();
        attemptWrongPassword();

        // Still active before the 3rd failure.
        Patron beforeLock = reload();
        assertThat(beforeLock.getStatus()).isEqualTo(PatronStatus.ACTIVE.getCode());
        assertThat(beforeLock.getLockedUntil()).isNull();

        attemptWrongPassword(); // 3rd failure -> lock

        Patron locked = reload();
        assertThat(locked.getFailedLoginCount()).isEqualTo((short) 3);
        assertThat(locked.getStatus()).isEqualTo(9);
        assertThat(locked.getStatus()).isEqualTo(PatronStatus.LOCKED.getCode());
        assertThat(locked.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    void lockedAccountRejectsFurtherLoginWithAccountLockedError() {
        attemptWrongPassword();
        attemptWrongPassword();
        attemptWrongPassword(); // locked now

        LoginRequest correct = new LoginRequest();
        correct.setEmail(EMAIL);
        correct.setPassword(CORRECT_PASSWORD);

        // Even the correct password is refused while locked, with a clean
        // ACCOUNT_LOCKED service error (maps to HTTP 423).
        assertThatThrownBy(() -> authService.login(correct))
                .isInstanceOf(ServiceException.class)
                .satisfies(ex -> assertThat(((ServiceException) ex).getErrorCode())
                        .contains(ErrorCode.ACCOUNT_LOCKED.getErrorCode()));
    }

    @Test
    void unlockRestoresActiveStatusCountAndLock() {
        attemptWrongPassword();
        attemptWrongPassword();
        attemptWrongPassword(); // locked

        patronService.unlockPatron(patronId, "ADMIN:admin@test.com");

        Patron unlocked = reload();
        assertThat(unlocked.getStatus()).isEqualTo(PatronStatus.ACTIVE.getCode());
        assertThat(unlocked.getFailedLoginCount()).isEqualTo((short) 0);
        assertThat(unlocked.getLockedUntil()).isNull();
        assertThat(unlocked.getModifiedBy()).isEqualTo("ADMIN:admin@test.com");
    }

    private Patron reload() {
        return patronRepository.findById(patronId).orElseThrow();
    }
}
