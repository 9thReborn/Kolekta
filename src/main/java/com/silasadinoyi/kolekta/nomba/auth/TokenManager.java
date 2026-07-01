package com.silasadinoyi.kolekta.nomba.auth;

import com.silasadinoyi.kolekta.config.NombaProperties;
import com.silasadinoyi.kolekta.nomba.auth.dto.TokenResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Owns the Nomba access token lifecycle.
 * Why: tokens live 60 min. Fetching per call is wasteful; reusing forever fails
 * after expiry. So we cache ONE token and refresh it at the ~55-min mark.
 * Thread-safe: a volatile snapshot on the fast path + a lock with double-check
 * means only one thread ever refreshes while others reuse the cached token.
 * Scale: in-memory is right for one node; multi-node => move to Redis.
 */
@Component
public class TokenManager {
    private static final Logger log = LoggerFactory.getLogger(TokenManager.class);
    private static final long DEFAULT_TTL_SECONDS = 3600;

    private final NombaAuthClient authClient;
    private final NombaProperties props;
    private final ReentrantLock lock = new ReentrantLock();
    private volatile CachedToken cached;

    public TokenManager(NombaAuthClient authClient, NombaProperties props) {
        this.authClient = authClient;
        this.props = props;
    }
    public String getAccessToken() {
        CachedToken snapshot = cached;
        if (isFresh(snapshot)) {
            return snapshot.accessToken();
        }
        lock.lock();
        try {
            if (isFresh(cached)) {            // re-check: another thread may have refreshed
                return cached.accessToken();
            }
            cached = fetchFreshToken();
            return cached.accessToken();
        } finally {
            lock.unlock();
        }
    }
    /** Force a refresh on next call, e.g. after a 401. */
    public void invalidate() {
        cached = null;
    }
    private boolean isFresh(CachedToken token) {
        return token != null && Instant.now().isBefore(token.refreshAt());
    }
    private CachedToken fetchFreshToken() {
        TokenResponse response = authClient.issueToken();
        if (response == null || response.data() == null || response.data().accessToken() == null) {
            throw new IllegalStateException("Nomba returned no access_token: " + response);
        }
        long ttl = parseTtlSeconds(response.data().expiresIn());
        Instant refreshAt = Instant.now().plusSeconds(ttl).minusSeconds(props.tokenRefreshMarginSeconds());
        log.debug("Issued new Nomba token; ttl={}s, refresh at {}", ttl, refreshAt);
        return new CachedToken(response.data().accessToken(), refreshAt);
    }
    private long parseTtlSeconds(String expiresIn) {
        if (expiresIn == null || expiresIn.isBlank()) {
            return DEFAULT_TTL_SECONDS;
        }
        try {
            return Long.parseLong(expiresIn.trim());
        } catch (NumberFormatException e) {
            log.warn("Unparseable expires_in '{}', defaulting to {}s", expiresIn, DEFAULT_TTL_SECONDS);
            return DEFAULT_TTL_SECONDS;
        }
    }
    private record CachedToken(String accessToken, Instant refreshAt) {}
}
