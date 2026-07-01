package com.silasadinoyi.kolekta.common;

public final class Money {
    private Money() { }

    /** Format kobo as naira using integer math — never floats for money. */
    public static String formatKobo(long kobo) {
        long naira = kobo / 100;
        long k = Math.abs(kobo % 100);
        return String.format("\u20a6%,d.%02d", naira, k);   // \u20a6 = ₦
    }
}