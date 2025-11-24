package com.schwitzer.schwitzersHelp.util;

public class NumberUtil {

    public static String convertPriceString(String priceString) {
        priceString = priceString.toLowerCase().trim();

        if (priceString.endsWith("k")) {
            // Entferne 'k' und multipliziere mit 1000
            String numberPart = priceString.substring(0, priceString.length() - 1);
            try {
                double value = Double.parseDouble(numberPart);
                return String.valueOf((long)(value * 1000));
            } catch (NumberFormatException e) {
                return priceString; // Fallback bei Parsing-Fehler
            }
        } else if (priceString.endsWith("m")) {
            // Entferne 'm' und multipliziere mit 1000000
            String numberPart = priceString.substring(0, priceString.length() - 1);
            try {
                double value = Double.parseDouble(numberPart);
                return String.valueOf((long)(value * 1000000));
            } catch (NumberFormatException e) {
                return priceString; // Fallback bei Parsing-Fehler
            }
        }

        // Wenn kein k oder m, gib den ursprünglichen String zurück
        return priceString;
    }
}
