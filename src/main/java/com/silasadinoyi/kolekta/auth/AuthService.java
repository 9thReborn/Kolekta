package com.silasadinoyi.kolekta.auth;

import com.silasadinoyi.kolekta.domain.merchant.Merchant;
import com.silasadinoyi.kolekta.domain.user.AppUser;
import com.silasadinoyi.kolekta.domain.user.Role;
import com.silasadinoyi.kolekta.domain.user.UserRepository;
import com.silasadinoyi.kolekta.provisioning.ProvisioningService;
import com.silasadinoyi.kolekta.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository users;
    private final ProvisioningService provisioning;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, ProvisioningService provisioning,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users; this.provisioning = provisioning;
        this.passwordEncoder = passwordEncoder; this.jwtService = jwtService;
    }

    @Transactional
    public AuthResult register(String email, String password, String businessName) {
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmail(normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        Merchant merchant = provisioning.createMerchant(businessName);
        AppUser user = users.save(new AppUser(
                normalized, passwordEncoder.encode(password), Role.MERCHANT, merchant.getId()));
        return toResult(user);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String password) {
        String normalized = email.trim().toLowerCase();
        AppUser user = users.findByEmail(normalized)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return toResult(user);
    }

    private AuthResult toResult(AppUser user) {
        String token = jwtService.issue(user.getId(), user.getRole().name(), user.getMerchantId());
        return new AuthResult(token, user.getRole().name(), user.getEmail(), user.getMerchantId());
    }

    public record AuthResult(String token, String role, String email, UUID merchantId) {}
}