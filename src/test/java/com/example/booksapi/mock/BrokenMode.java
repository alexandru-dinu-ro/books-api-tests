package com.example.booksapi.mock;

import java.util.Arrays;

/**
 * Deliberate defects the mock can simulate (enabled with -Dmock.broken=<value>).
 * Used to prove that the test suite detects regressions in each requirement.
 */
public enum BrokenMode {

    /**
     * A correct mock.
     */
    NONE("none"),

    /**
     * Duplicate books are accepted instead of rejected.
     */
    DUPLICATES("duplicates"),

    /**
     * Empty title/author values are accepted instead of rejected.
     */
    EMPTY_FIELDS("empty-fields"),

    /**
     * The read-only id can be set by the client.
     */
    WRITABLE_ID("writable-id"),

    /**
     * Creating a book fails with HTTP 500.
     */
    SERVER_ERROR("server-error");

    private final String value;

    BrokenMode(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    /**
     * Converts the -Dmock.broken value into an enum constant, failing fast on typos.
     */
    public static BrokenMode fromValue(String value) {
        return Arrays.stream(values())
                .filter(mode -> mode.value.equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown mock.broken value '" + value + "'. Allowed: "
                                + Arrays.stream(values()).map(BrokenMode::value).toList()));
    }
}
