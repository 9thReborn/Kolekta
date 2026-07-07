package com.silasadinoyi.kolekta.nomba.virtualaccount;

/** The port: "give a customer a dedicated account." Implemented by Nomba (real) or a mock. */
public interface VirtualAccountProvider {

    ProvisionedAccount create(CreateCommand command);
    void expire(String accountRef);

    /** What we ask the provider to create. */
    record CreateCommand(
            String accountRef,
            String accountName,
            String currency,
            String bvn,         // nullable — inherits parent BVN if null
            String expiryDate   // nullable — null => static / permanent account
    ) {}

    /** What the provider returns once the NUBAN exists. */
    record ProvisionedAccount(
            String accountNumber,
            String bankName,
            String accountName
    ) {}
}