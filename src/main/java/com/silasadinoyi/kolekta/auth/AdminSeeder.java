package com.silasadinoyi.kolekta.auth;

import com.silasadinoyi.kolekta.domain.user.AppUser;
import com.silasadinoyi.kolekta.domain.user.Role;
import com.silasadinoyi.kolekta.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UserRepository users, PasswordEncoder passwordEncoder,
                       @Value("${admin.email:}") String adminEmail,
                       @Value("${admin.password:}") String adminPassword) {
        this.users = users; this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail; this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.info("Admin seed skipped (ADMIN_EMAIL/ADMIN_PASSWORD not set)");
            return;
        }
        String email = adminEmail.trim().toLowerCase();
        if (users.existsByEmail(email)) return;
        users.save(new AppUser(email, passwordEncoder.encode(adminPassword), Role.ADMIN, null));
        log.info("Seeded ADMIN user {}", email);
    }
}