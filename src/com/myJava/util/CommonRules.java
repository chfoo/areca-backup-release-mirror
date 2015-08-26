package com.myJava.util;

/**
 * Utility class used to check common rules : non-nullity, numeric format, emails, 
 * password strength, ...
 * <BR>
 * @author Olivier PETRUCCI
 * <BR>
 *
 */

 /*
 Copyright 2005-2015, Olivier PETRUCCI.

This file is part of Areca.

    Areca is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    Areca is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Areca; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 */

public abstract class CommonRules {

    public static boolean checkEmptyString(Object value) {
        return (value == null || value.toString().trim().equals(""));
    }

    public static boolean checkDouble(String value, boolean checkPositiveValue) {
        try {
            double d = Double.parseDouble(value);
            return (d >= 0 || !checkPositiveValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean checkDouble(String value, double minValue, double maxValue) {
        try {
            double d = Double.parseDouble(value);
            return (d >= minValue && d <= maxValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean checkDouble(String value, double maxValue) {
        try {
            double d = Double.parseDouble(value);
            return (d <= maxValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean checkDouble(String value) {
        return CommonRules.checkDouble(value, false);
    }

    public static boolean checkInteger(String value, boolean checkPositiveValue) {
        try {
            int i = Integer.parseInt(value);
            return (i >= 0 || !checkPositiveValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean checkInteger(String value, int minValue, int maxValue) {
        try {
            int i = Integer.parseInt(value);
            return (i >= minValue && i <= maxValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean checkInteger(String value, int maxValue) {
        try {
            int i = Integer.parseInt(value);
            return (i <= maxValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static boolean checkInteger(String value) {
        return CommonRules.checkInteger(value, false);
    }

    public static boolean checkMaxLength(Object value, int maxLength) {
        return (value == null || value.toString().length() <= maxLength);
    }

    /**
     * - not null
     * <BR>- contains a "@"
     */
    public static boolean checkEmail(String value) {
        if (value == null) {
            return false;
        } else {
            return (value.indexOf("@") != -1);
        }
    }

    public static boolean checkPasswordStrength(
                                                    String value,
                                                    String oldValue,
                                                    int minimumNonAlpha,
                                                    int minimumNonAlphaNumeric,
                                                    int minLength) {

        if (value == null || value.length() < minLength || value.equalsIgnoreCase(oldValue)) {
            return false;
        } else {
            String alpha = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
            String numeric = "0123456789";

            int nbNonAlpha = 0;
            int nbNonAlphaNum = 0;
            for (int i=0; i<value.length(); i++) {
                char c = value.charAt(i);
                if (alpha.indexOf(c) == -1) {
                    nbNonAlpha++;
                    if (numeric.indexOf(c) == -1) {
                        nbNonAlphaNum++;
                    }
                }
            }

            return (nbNonAlpha >= minimumNonAlpha && nbNonAlphaNum >= minimumNonAlphaNumeric);
        }
    }

    public static boolean checkPasswordStrength(
                                                    String value,
                                                    int minimumNonAlpha,
                                                    int minimumNonAlphaNumeric,
                                                    int minLength) {

        return CommonRules.checkPasswordStrength(value, "", minimumNonAlpha, minimumNonAlphaNumeric, minLength);
    }
}