package com.dolimcom.semanticrouter.support;

import java.util.Locale;

public final class TextSupport {

    private TextSupport() {
    }

    public static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public static String preview(String value, int maxLength) {
        if (maxLength <= 0) {
            return "";
        }
        String normalized = value == null ? "" : value.replaceAll("\\s+", " ").trim();
        if (normalized.length() <= maxLength) {
            return normalized;
        }
        return normalized.substring(0, maxLength) + "...";
    }
}
