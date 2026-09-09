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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    @Value("${auth.max-failed-attempts:5}")
    private short maxFailedAttempts;

    @Value("${auth.lockout-duration-minutes:15}")
    private long lockoutDurationMinutes;

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

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(prefixedUsername, rawPassword));
        } catch (BadCredentialsException ex) {
            recordFailedAttempt(patron);
            throw ex;
        }

        // Successful login — reset failure counter
        patron.setFailedLoginCount((short) 0);
        patron.setLockedUntil(null);
        patronRepository.save(patron);

        UserDetails userDetails = userDetailsService.loadUserByUsername(prefixedUsername);
        String role = UserRole.PATRON.name();
        String token = tokenService.generateToken(userDetails, role);

        return LoginResponse.builder()
                .accessToken(token)
                .expiresIn(jwtConfig.expirationMs() / 1000)
                .userId(patron.getPatronId())
                .name(patron.getName())
                .email(patron.getEmail())
                .role(role)
                .build();
    }

    private void recordFailedAttempt(Patron patron) {
        short attempts = (short) (patron.getFailedLoginCount() + 1);
        patron.setFailedLoginCount(attempts);

        if (attempts >= maxFailedAttempts) {
            patron.setLockedUntil(LocalDateTime.from(Instant.now().plusSeconds(lockoutDurationMinutes * 60)));
        }

        patronRepository.save(patron);
    }

    private LoginResponse authenticateAdmin(AdminUser admin, String rawPassword) {
        String prefixedUsername = "ADMIN:" + admin.getEmail();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(prefixedUsername, rawPassword));

        UserDetails userDetails = userDetailsService.loadUserByUsername(prefixedUsername);
        String role = UserRole.ADMIN.name();
        String token = tokenService.generateToken(userDetails, role);

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
