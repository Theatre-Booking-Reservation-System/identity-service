package com.theatre.identityservice.config;

import com.theatre.identityservice.repository.model.AdminUser;
import com.theatre.identityservice.repository.model.Patron;
import com.theatre.identityservice.repository.AdminUserRepository;
import com.theatre.identityservice.repository.PatronRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final PatronRepository patronRepository;
    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedPatrons();
        seedAdminUsers();
    }

    private void seedPatrons() {
        if (patronRepository.count() > 0) return;

        patronRepository.save(Patron.builder()
                .name("Isuru Wijegunasinghe")
                .email("isuruwijegunasinghe@gmail.com")
                .passwordHash(passwordEncoder.encode("Isuru@1234"))
                .isVerified(true)
                .build());

        log.info("Seeded PATRON: isuruwijegunasinghe@gmail.com");
    }

    private void seedAdminUsers() {
        if (adminUserRepository.count() > 0) return;

        adminUserRepository.save(AdminUser.builder()
                .name("Theatre Admin")
                .email("admin@sapumaltheatre.com")
                .passwordHash(passwordEncoder.encode("Admin@1234"))
                .build());

        log.info("Seeded ADMIN_USER: admin@sapumaltheatre.com (ADMIN)");
    }
}
