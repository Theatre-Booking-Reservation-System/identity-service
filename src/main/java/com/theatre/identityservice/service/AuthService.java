package com.theatre.identityservice.service;

import com.theatre.identityservice.exception.ServiceException;
import com.theatre.identityservice.model.LoginRequest;
import com.theatre.identityservice.model.LoginResponse;
import com.theatre.identityservice.repository.model.AdminUser;
import com.theatre.identityservice.repository.model.Patron;
import com.theatre.identityservice.repository.AdminUserRepository;
import com.theatre.identityservice.repository.PatronRepository;
import com.theatre.identityservice.config.JwtConfig;
import com.theatre.identityservice.util.ErrorCode;
import com.theatre.identityservice.util.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final PatronRepository patronRepository;
    private final AdminUserRepository adminUserRepository;
    private final UserDetailsService userDetailsService;
    private final TokenService tokenService;
    private final JwtConfig jwtConfig;
    private final PatronLoginAttemptService patronLoginAttemptService;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();

        Optional<Patron> maybePatron = patronRepository.findByEmail(email);
        Optional<AdminUser> maybeAdmin = adminUserRepository.findByEmail(email);

        if (maybePatron.isEmpty() && maybeAdmin.isEmpty()) {
            throw new ServiceException(ErrorCode.INVALID_USER_ID_OR_PASSWORD);
        }

        if (maybePatron.isPresent()) {
            return authenticatePatron(maybePatron.get(), request.getPassword());
        }

        return authenticateAdmin(maybeAdmin.get(), request.getPassword());
    }

    private LoginResponse authenticatePatron(Patron patron, String rawPassword) {
        String prefixedUsername = "PATRON:" + patron.getEmail();

        if (patron.getLockedUntil() != null && patron.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new ServiceException(ErrorCode.ACCOUNT_LOCKED);
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(prefixedUsername, rawPassword));
        } catch (BadCredentialsException ex) {
            patronLoginAttemptService.recordFailedAttempt(patron.getPatronId());
            throw ex;
        }

        // Successful login — reset the failure counter (separate transaction).
        patronLoginAttemptService.resetFailedAttempts(patron.getPatronId());

        UserDetails userDetails = userDetailsService.loadUserByUsername(prefixedUsername);
        String role = UserRole.PATRON.name();
        String token = tokenService.generateToken(userDetails, role, patron.getEmail());

        return LoginResponse.builder()
                .accessToken(token)
                .expiresIn(jwtConfig.expirationMs() / 1000)
                .userId(patron.getPatronId())
                .name(patron.getName())
                .email(patron.getEmail())
                .role(role)
                .build();
    }

    private LoginResponse authenticateAdmin(AdminUser admin, String rawPassword) {
        String prefixedUsername = "ADMIN:" + admin.getEmail();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(prefixedUsername, rawPassword));

        UserDetails userDetails = userDetailsService.loadUserByUsername(prefixedUsername);
        String role = UserRole.ADMIN.name();
        String token = tokenService.generateToken(userDetails, role, admin.getEmail());

        return LoginResponse.builder()
                .accessToken(token)
                .expiresIn(jwtConfig.expirationMs() / 1000)
                .userId(admin.getAdminId())
                .name(admin.getName())
                .email(admin.getEmail())
                .role(role)
                .build();
    }
}
