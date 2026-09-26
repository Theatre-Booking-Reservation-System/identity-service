package com.theatre.identityservice.service;

import com.theatre.identityservice.exception.ServiceException;
import com.theatre.identityservice.model.PatronListResponse;
import com.theatre.identityservice.model.PatronRegisterRequest;
import com.theatre.identityservice.model.PatronRegisterResponse;
import com.theatre.identityservice.model.PatronSummary;
import com.theatre.identityservice.repository.PatronRepository;
import com.theatre.identityservice.repository.model.Patron;
import com.theatre.identityservice.util.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
                .isVerified(false)
                .status(1)
                .addedBy(email)
                .addedDate(LocalDateTime.now())
                .build();

        Patron saved = patronRepository.save(patron);

        return PatronRegisterResponse.builder()
                .patronId(saved.getPatronId())
                .name(saved.getName())
                .email(saved.getEmail())
                .contactNo(saved.getContactNo())
                .dateOfBirth(saved.getDateOfBirth())
                .nicPassportNo(saved.getNicPassportNo())
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
