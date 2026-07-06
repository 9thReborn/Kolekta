package com.silasadinoyi.kolekta.security;
import java.util.UUID;
public record AuthPrincipal(UUID userId, UUID merchantId, String role) {}