package org.example.javafxdb_sql_shellcode;

import java.util.regex.Pattern;


public final class Validator {

    private Validator() { }

    /**
     * Names: start with a letter, then letters, spaces, apostrophes or hyphens.
     * 2 to 25 characters total. Examples that pass: "Jean", "O'Neil", "Mary-Ann".
     */
    public static final Pattern NAME =
            Pattern.compile("^[A-Za-z][A-Za-z' -]{1,24}$");

    /**
     * Department / major: letters, digits, spaces, & . - allowed.
     * 2 to 30 characters. Examples: "CS", "Computer Science", "CPIS".
     */
    public static final Pattern DEPT_MAJOR =
            Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 &.-]{1,29}$");

    /**
     * Email: something@something.domain
     * - before @ : letters, digits, dot, underscore, plus, hyphen
     * - after  @ : domain parts separated by dots, ending in 2+ letters
     */
    public static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._+-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");

    /**
     * Password (registration page): at least 8 characters with at least
     * one letter and one digit. (?=...) are "lookaheads" that check a
     * condition without consuming characters.
     */
    public static final Pattern PASSWORD =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d).{8,}$");

    /** Returns true if the (trimmed) text fully matches the pattern. */
    public static boolean isValid(Pattern pattern, String text) {
        return text != null && pattern.matcher(text.trim()).matches();
    }
}
