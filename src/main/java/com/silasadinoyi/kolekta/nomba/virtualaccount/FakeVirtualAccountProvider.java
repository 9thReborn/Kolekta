package com.silasadinoyi.kolekta.nomba.virtualaccount;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stand-in used until sandbox credentials arrive (nomba.mock=true, the default).
 * Lets us build and test the whole flow locally. Deterministic fake NUBAN from accountRef.
 */
@Component
@ConditionalOnProperty(name = "nomba.mock", havingValue = "true", matchIfMissing = true)
public class FakeVirtualAccountProvider implements VirtualAccountProvider {

    private static final Logger log = LoggerFactory.getLogger(FakeVirtualAccountProvider.class);

    @Override
    public ProvisionedAccount create(CreateCommand cmd) {
        String number = "99" + String.format("%08d", Math.abs(cmd.accountRef().hashCode()) % 100_000_000);
        log.info("[MOCK] Issued fake virtual account {} for ref {}", number, cmd.accountRef());
        return new ProvisionedAccount(number, "Nombank MFB (Sandbox-Mock)", cmd.accountName());
    }
}