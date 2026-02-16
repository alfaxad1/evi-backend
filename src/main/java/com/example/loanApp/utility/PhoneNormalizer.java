package com.example.loanApp.utility;

public class PhoneNormalizer {
    private static final String COUNTRY_CODE = "254";

    private PhoneNormalizer() {
        // utility class
    }

    public static String normalize(String phone) {

        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        // Remove spaces, dashes, brackets
        phone = phone.replaceAll("[^0-9+]", "");

        // 0712345678 OR 0112345678 → +2547... / +2541...
        if (phone.startsWith("0") && phone.length() == 10) {
            char secondDigit = phone.charAt(1);
            if (secondDigit == '7' || secondDigit == '1') {
                return "+254" + phone.substring(1);
            }
        }

        // 712345678 OR 112345678 → +2547... / +2541...
        if (phone.length() == 9) {
            char firstDigit = phone.charAt(0);
            if (firstDigit == '7' || firstDigit == '1') {
                return "+254" + phone;
            }
        }

        // 2547XXXXXXXX OR 2541XXXXXXXX → +2547... / +2541...
        if (phone.startsWith("254") && phone.length() == 12) {
            char fourthDigit = phone.charAt(3);
            if (fourthDigit == '7' || fourthDigit == '1') {
                return "+" + phone;
            }
        }

        // +2547XXXXXXXX OR +2541XXXXXXXX → OK
        if (phone.startsWith("+254") && phone.length() == 13) {
            char fifthDigit = phone.charAt(4);
            if (fifthDigit == '7' || fifthDigit == '1') {
                return phone;
            }
        }

        throw new IllegalArgumentException("Invalid Kenyan phone number format");
    }
}
