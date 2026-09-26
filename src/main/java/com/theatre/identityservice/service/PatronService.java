package com.theatre.identityservice.service;

import com.theatre.identityservice.exception.ServiceException;
import com.theatre.identityservice.model.CommonResponse;
import com.theatre.identityservice.model.PatronDetailResponse;
import com.theatre.identityservice.model.PatronListResponse;
import com.theatre.identityservice.model.PatronRegisterRequest;
import com.theatre.identityservice.model.PatronRegisterResponse;
import com.theatre.identityservice.model.PatronSummary;
import com.theatre.identityservice.repository.PatronRepository;
import com.theatre.identityservice.repository.model.Patron;
import com.theatre.identityservice.repository.spec.PatronSpecifications;
import com.theatre.identityservice.util.ErrorCode;
import com.theatre.identityservice.util.PatronStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatronService {

    private final PatronRepository patronRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public PatronRegisterResponse register(PatronRegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        if (patronRepository.findByEmail(email).isPresent()) {
            throw new ServiceException(ErrorCode.EMAIL_ALREADY_REGISTERED);
        }

        Patron patron = Patron.builder()
                .name(request.getName().trim())
                .email(email)
                .contactNo(request.getContactNo() != null ? request.getContactNo().trim() : null)
                .dateOfBirth(request.getDateOfBirth())
                .nicPassportNo(request.getNicPassportNo() != null ? request.getNicPassportNo().trim() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isVerified(true)
                .status(PatronStatus.ACTIVE.getCode())
                .addedBy(email)
                .addedDate(LocalDateTime.now())
                .build();

        Patron saved = patronRepository.save(patron);

        return PatronRegisterResponse.builder()
                .statusCode("SUCCESS")
                .statusDescription("Patron registered successfully")
                .patronId(saved.getPatronId())
                .email(saved.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public PatronListResponse listAllPatrons() {
        List<PatronSummary> patrons = patronRepository
                .findAll(Sort.by(Sort.Direction.DESC, "addedDate"))
                .stream()
                .map(this::toSummary)
                .toList();

        return PatronListResponse.builder()
                .totalCount(patrons.size())
                .patrons(patrons)
                .build();
    }

    @Transactional(readOnly = true)
    public PatronListResponse searchPatrons(String name, String email, Boolean loyaltyHolder, Integer status) {
        Specification<Patron> spec = Specification
                .allOf(
                        PatronSpecifications.nameContains(name),
                        PatronSpecifications.emailContains(email),
                        PatronSpecifications.loyaltyHolderIs(loyaltyHolder),
                        PatronSpecifications.statusIs(status));

        List<PatronSummary> patrons = patronRepository
                .findAll(spec, Sort.by(Sort.Direction.DESC, "addedDate"))
                .stream()
                .map(this::toSummary)
                .toList();

        return PatronListResponse.builder()
                .totalCount(patrons.size())
                .patrons(patrons)
                .build();
    }

    @Transactional(readOnly = true)
    public PatronDetailResponse getPatron(UUID patronId) {
        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PATRON_NOT_FOUND));

        return PatronDetailResponse.builder()
                .patron(toSummary(patron))
                .build();
    }

    @Transactional
    public CommonResponse unlockPatron(UUID patronId, String performedBy) {
        Patron patron = patronRepository.findById(patronId)
                .orElseThrow(() -> new ServiceException(ErrorCode.PATRON_NOT_FOUND));

        // Restore the account to a usable state: clear the lock, reset the
        // failure counter and set status back to ACTIVE.
        patron.setStatus(PatronStatus.ACTIVE.getCode());
        patron.setLockedUntil(null);
        patron.setFailedLoginCount((short) 0);
        patron.setModifiedBy(performedBy);
        patron.setModifiedDate(LocalDateTime.now());
        patronRepository.save(patron);

        return CommonResponse.builder()
                .statusCode("SUCCESS")
                .statusDescription("Patron account unlocked")
                .build();
    }

    private PatronSummary toSummary(Patron patron) {
        return PatronSummary.builder()
                .patronId(patron.getPatronId())
                .name(patron.getName())
                .email(patron.getEmail())
                .contactNo(patron.getContactNo())
                .dateOfBirth(patron.getDateOfBirth())
                .nicPassportNo(patron.getNicPassportNo())
                .verified(patron.isVerified())
                .loyaltyCardNo(patron.getLoyaltyCardNo())
                .loyaltyHolder(patron.isLoyaltyHolder())
                .status(patron.getStatus())
                .addedDate(patron.getAddedDate())
                .build();
    }
}
