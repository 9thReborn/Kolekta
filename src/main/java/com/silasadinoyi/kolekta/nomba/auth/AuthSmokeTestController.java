package com.silasadinoyi.kolekta.nomba.auth;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal")
public class AuthSmokeTestController {
    private final TokenManager tokenManager;

    public AuthSmokeTestController(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    @GetMapping("/token-check")
    public Map<String, Object> tokenCheck() {
        String token = tokenManager.getAccessToken();
        return Map.of("acquired", true, "tokenPreview", mask(token));
    }

    private String mask(String token) {
        if (token == null || token.length() < 8) return "****";
        return token.substring(0, 4) + "…" + token.substring(token.length() - 4);
    }
}
