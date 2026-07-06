package com.silasadinoyi.kolekta.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @PostMapping("/register")
    public AuthService.AuthResult register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req.email(), req.password(), req.businessName());
    }

    @PostMapping("/login")
    public AuthService.AuthResult login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req.email(), req.password());
    }

    public record RegisterRequest(@Email @NotBlank String email,
                                  @NotBlank @Size(min = 6) String password,
                                  @NotBlank String businessName) {}
    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}
}