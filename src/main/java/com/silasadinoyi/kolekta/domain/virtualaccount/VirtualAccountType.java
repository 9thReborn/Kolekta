package com.silasadinoyi.kolekta.domain.virtualaccount;

public enum VirtualAccountType {
    STATIC,   // permanent, per-customer dedicated account (our primary mode)
    DYNAMIC   // one-time / time-boxed (e.g. an invoice with an expiry)
}
