/* Copyright (c) 2014 Karol Stasiak, All Rights Reserved
*
* This library is free software; you can redistribute it and/or
* modify it under the terms of the GNU Lesser General Public
* License as published by the Free Software Foundation; either
* version 3 of the License, or (at your option) any later version.
*
* This library is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*/

package io.github.karols.hocr4j.utils;

import java.util.regex.Pattern;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Various utility functions for dealing with text.
 */
public class TextUtils {

    private static final String tab00c0 =
            "AAAAAAACEEEEIIII" +
                    "DNOOOOOxOUUUUYTs" +
                    "aaaaaaaceeeeiiii" +
                    "dnooooo/ouuuuyty" +
                    "AaAaAaCcCcCcCcDd" +
                    "DdEeEeEeEeEeGgGg" +
                    "GgGgHhHhIiIiIiIi" +
                    "IiJjJjKkkLlLlLlL" +
                    "lLlNnNnNnnNnOoOo" +
                    "OoOoRrRrRrSsSsSs" +
                    "SsTtTtTtUuUuUuUu" +
                    "UuUuWwYyYZzZzZzs";
    private static Pattern SMALLER = Pattern.compile("^([,‚„\\._ ]*|[\"\'‘“”’\\^ ]*)$");

    private static int fuzzyConsume(@Nonnull String s1, @Nonnull String s2) {
        int i1 = 0;
        int i2 = 0;
        boolean canSkip1 = true;
        boolean canSkip2 = true;
        while (i1 < s1.length() && i2 < s2.length()) {
            boolean notMoved = true;
            char c1 = s1.charAt(i1);
            char c2 = s2.charAt(i2);
            if (fuzzyEqual(c1, c2)) {
                i1++;
                i2++;
                canSkip1 = true;
                canSkip2 = true;
                notMoved = false;
            } else {
                if (canSkip1 && isNegligible(c1)) {
                    canSkip1 = false;
                    i1++;
                    notMoved = false;
                }
                if (canSkip2 && isNegligible(c2)) {
                    canSkip2 = false;
                    i2++;
                    notMoved = false;
                }
            }
            //TODO: °/o == %
            if (notMoved) {
                break;
            }
        }
        if (i1 < s1.length() && canSkip1) {
            if (isNegligible(s1.charAt(i1))) {
                i1++;
            }
        }
        if (i2 < s2.length() && canSkip2) {
            if (isNegligible(s2.charAt(i2))) {
                i2++;
            }
        }
        if (i2 == s2.length()) {
            return s1.length() - i1;
        } else {
            return -1;
        }
    }

    /**
     * Checks if the needle string
     * is actually a substring of the given haystack string,
     * using the same equality as <code>fuzzyEqual</code>.
     *
     * @param haystack the long string
     * @param needle   potential substring
     * @return <code>true</code> if <code>needle</code>
     *         is a fuzzy substring of <code>haystack</code>, <code>false</code> otherwise
     * @see TextUtils#fuzzyEqual(String, String)
     */
    public static boolean fuzzyContains(@Nonnull String haystack, @Nonnull String needle) {
        for (int i = 0; i < haystack.length(); i++) {
            if (fuzzyPrefix(haystack.substring(i), needle)) {
                return true;
            }
        }
        return false;
    }

    private static boolean fuzzyEqual(char c1, char c2) {
        c1 = toAscii(c1);
        c2 = toAscii(c2);
        if (c1 == c2) return true;
        if ("lI1".indexOf(c1) >= 0 && "lI1".indexOf(c2) >= 0) return true;
        if ("0Oo".indexOf(c1) >= 0 && "0Oo".indexOf(c2) >= 0) return true;
        if (",.".indexOf(c1) >= 0 && ",.".indexOf(c2) >= 0) return true;
        if ("-–".indexOf(c1) >= 0 && "-–".indexOf(c2) >= 0) return true;
        if ("„“”\"".indexOf(c1) >= 0 && "„“”\"".indexOf(c2) >= 0) return true;
        //TODO
        return false;
    }

    /**
     * Checks if the strings are approximately equal.
     * <br/>
     * Strings are approximately equal
     * if their corresponding characters are approximately equal,
     * optionally separated by a single negligible character.
     * <br/>
     * Characters are approximately if at least one of the following is true:
     * <ul>
     * <li>they are equal</li>
     * <li>they are equal after running through <code>toAscii</code></li>
     * <li>they both belong to the set: uppercase I, lowercase ell, Arabic digit one</li>
     * <li>they both belong to the set: uppercase O, lowercase o, Arabic digit zero</li>
     * <li>they are both double quote marks, regardless of their kind</li>
     * <li>one is an ASCII hyphen and the other is an n-dash</li>
     * <li>one is an ASCII period and the other is an ASCII comma</li>
     * </ul>
     * <br/>
     * Negligible characters are single quote marks and ASCII commas.
     *
     * @param s1 first string
     * @param s2 second string
     * @return <code>true</code> if the strings are approximately equal, <code>false</code> otherwise
     */
    public static boolean fuzzyEqual(@Nullable String s1, @Nullable String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s2 != null && 0 == fuzzyConsume(s1, s2);
    }//tested

    /**
     * Checks if the potential prefix string
     * is actually a prefix of the given string,
     * using the same equality as <code>fuzzyEqual</code>.
     *
     * @param str    the long string
     * @param prefix potential prefix
     * @return <code>true</code> if <code>prefix</code>
     *         is a fuzzy prefix of <code>str</code>, <code>false</code> otherwise
     * @see TextUtils#fuzzyEqual(String, String)
     */
    public static boolean fuzzyPrefix(@Nonnull String str, @Nonnull String prefix) {
        return fuzzyConsume(str, prefix) >= 0;
    }//tested

    private static boolean isNegligible(char c) {
        return " \'‚,‘’".indexOf(c) >= 0;
    }

    /**
     * Checks if the string is smaller than most other strings.
     * A smaller string is one of the following:
     * <ul>
     * <li>an empty string</li>
     * <li>a string consisting of only spaces</li>
     * <li>a string consisting only of punctuation marks that occur near the baseline, with optional spaces</li>
     * <li>a string consisting only of punctuation marks that occur near the top, with optional spaces</li>
     * </ul>
     *
     * @param s string to check
     * @return <code>true</code> if the string is considered to be smaller, <code>false</code> otherwise
     */
    public static boolean isSmaller(@Nonnull String s) {
        return SMALLER.matcher(s).matches();
    }

    /**
     * Converts a Latin letter to its ASCII approximation.
     * Not a very smart algorithm. It only converts single letters into single letters, so for example:
     * <p/>
     * <ul>
     * <li>æ → a</li>
     * <li>œ → o</li>
     * <li>ð → d</li>
     * <li>đ → d</li>
     * <li>ß → s</li>
     * <li>þ → t</li>
     * <li>ü → u</li>
     * <li>× → x</li>
     * <li>÷ → /</li>
     * </ul>
     * <p/>
     * Only codepoints from U+00c0 &ndash; U+01FF range are converted,
     * all the other are returned as they are.
     *
     * @param source a character
     * @return if <code>source</code> was an accented Latin letter
     *         from the supported range, its ASCII equivalent,
     *         otherwise <code>source</code>
     */
    public static char toAscii(char source) {
        if (source >= 0xc0 && source <= 0x17f) {
            return tab00c0.charAt(source - 0xc0);
        }
        return source;
    }//tested

    private TextUtils() {
    }
}
