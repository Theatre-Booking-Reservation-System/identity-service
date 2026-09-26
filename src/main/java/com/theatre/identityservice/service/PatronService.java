package com.theatre.identityservice.service;

import com.theatre.identityservice.exception.ServiceException;
import com.theatre.identityservice.model.PatronRegisterRequest;
import com.theatre.identityservice.model.PatronRegisterResponse;
import com.theatre.identityservice.repository.PatronRepository;
import com.theatre.identityservice.repository.model.Patron;
import com.theatre.identityservice.util.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
                .build();
    }
}
