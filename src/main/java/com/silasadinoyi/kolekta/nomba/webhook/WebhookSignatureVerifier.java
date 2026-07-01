package com.silasadinoyi.kolekta.nomba.webhook;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/** Verifies nomba-signature: HMAC-SHA256(rawBody, secret), hex-encoded. */
@Component
public class WebhookSignatureVerifier {
    private static final Logger log = LoggerFactory.getLogger(WebhookSignatureVerifier.class);

    public boolean isValid(byte[] rawBody, String signatureHeader, String secret) {
        if (signatureHeader == null || secret == null || secret.isBlank()) {
            log.warn("Signature check skipped: headerPresent={}, secretPresent={}",
                    signatureHeader != null, secret != null && !secret.isBlank());
            return false;
        }
        String expected = hmacSha256Hex(rawBody, secret);
        // constant-time compare to avoid timing attacks
        boolean match = MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                signatureHeader.trim().getBytes(StandardCharsets.UTF_8));
        if (!match) {
            log.warn("Signature mismatch:\n  expected = {}\n  received = {}", expected, signatureHeader.trim());
        }
        return match;
    }

    private String hmacSha256Hex(byte[] body, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(body));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute HMAC", e);
        }
    }
}