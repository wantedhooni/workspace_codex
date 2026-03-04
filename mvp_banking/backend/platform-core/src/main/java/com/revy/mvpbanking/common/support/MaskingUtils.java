package com.revy.mvpbanking.common.support;

public final class MaskingUtils {

    private MaskingUtils() {
    }

    public static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return "***" + email.substring(Math.max(atIndex, 0));
        }

        String localPart = email.substring(0, atIndex);
        String domainPart = email.substring(atIndex);
        String visiblePrefix = localPart.substring(0, Math.min(2, localPart.length()));
        return visiblePrefix + "*".repeat(Math.max(localPart.length() - visiblePrefix.length(), 1)) + domainPart;
    }

    public static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }

    public static String maskAccountNumber(String accountNumber) {
        String digits = accountNumber.replaceAll("[^0-9]", "");
        if (digits.length() <= 4) {
            return "****";
        }

        String last4 = digits.substring(digits.length() - 4);
        return "***-***-" + last4;
    }
}
