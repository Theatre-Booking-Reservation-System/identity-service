package com.theatre.identityservice.service;

import com.theatre.identityservice.repository.PatronRepository;
import com.theatre.identityservice.repository.model.Patron;
import com.theatre.identityservice.util.PatronStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PatronLoginAttemptService {

    private final PatronRepository patronRepository;

    @Value("${auth.max-failed-attempts:5}")
    private short maxFailedAttempts;

    @Value("${auth.lockout-duration-minutes:15}")
    private long lockoutDurationMinutes;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailedAttempt(UUID patronId) {
        Patron patron = patronRepository.findById(patronId).orElse(null);
        if (patron == null) {
            return;
        }

        // If a previous lock has already elapsed, start a fresh failure cycle
        // from a clean, ACTIVE state.
        if (patron.getLockedUntil() != null && !patron.getLockedUntil().isAfter(LocalDateTime.now())) {
            patron.setStatus(PatronStatus.ACTIVE.getCode());
            patron.setLockedUntil(null);
            patron.setFailedLoginCount((short) 0);
        }

        short attempts = (short) (patron.getFailedLoginCount() + 1);
        patron.setFailedLoginCount(attempts);

        if (attempts >= maxFailedAttempts && patron.getStatus() != null
                && patron.getStatus() == PatronStatus.ACTIVE.getCode()) {
            patron.setStatus(PatronStatus.LOCKED.getCode());
            patron.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            patron.setModifiedBy("system-lockout");
            patron.setModifiedDate(LocalDateTime.now());
            log.info("Patron {} locked after {} failed login attempts", patron.getEmail(), attempts);
        }

        patronRepository.save(patron);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void resetFailedAttempts(UUID patronId) {
        Patron patron = patronRepository.findById(patronId).orElse(null);
        if (patron == null) {
            return;
        }

        if (patron.getFailedLoginCount() != 0 || patron.getLockedUntil() != null) {
            patron.setFailedLoginCount((short) 0);
            patron.setLockedUntil(null);
            patronRepository.save(patron);
        }
    }
}
