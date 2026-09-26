package com.theatre.identityservice.service;

import com.theatre.identityservice.repository.model.AdminUser;
import com.theatre.identityservice.repository.model.Patron;
import com.theatre.identityservice.repository.AdminUserRepository;
import com.theatre.identityservice.repository.PatronRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PatronRepository patronRepository;
    private final AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String prefixedEmail) throws UsernameNotFoundException {
        if (prefixedEmail.startsWith("PATRON:")) {
            String email = prefixedEmail.substring(7);
            Patron patron = patronRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Patron not found: " + email));

            enforcePatronLockout(patron);

            return buildUserDetails(
                    prefixedEmail,
                    patron.getPasswordHash(),
                    "ROLE_PATRON",
                    patron.isVerified()
            );
        }

        if (prefixedEmail.startsWith("ADMIN:")) {
            String email = prefixedEmail.substring(6);
            AdminUser admin = adminUserRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + email));

            return buildUserDetails(
                    prefixedEmail,
                    admin.getPasswordHash(),
                    "ROLE_ADMIN",
                    true
            );
        }

        throw new UsernameNotFoundException("Unknown user type for: " + prefixedEmail);
    }

    private void enforcePatronLockout(Patron patron) {
        if (patron.getLockedUntil() != null && patron.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new LockedException("Account is locked until " + patron.getLockedUntil());
        }
    }

    private UserDetails buildUserDetails(String username, String encodedPassword, String authority, boolean enabled) {
        if (!enabled) {
            throw new DisabledException("Account is not verified: " + username);
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password(encodedPassword)
                .authorities(List.of(new SimpleGrantedAuthority(authority)))
                .build();
    }
}
