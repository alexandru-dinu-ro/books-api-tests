package com.example.booksapi.model;

/**
 * Fixed test data, so failures are reproducible and easy to read.
 * Factory methods return a NEW Book each time, so tests can never share or modify the same object.
 */
public final class TestData {

    public static final String AUTHOR_JOHN = "John Smith";
    public static final String TITLE_SRE = "SRE 101";

    public static final String AUTHOR_JANE = "Jane Archer";
    public static final String TITLE_DEVOPS = "DevOps is a lie";

    private TestData() {
        // utility class, no instances
    }

    public static Book sre101() {
        return new Book(AUTHOR_JOHN, TITLE_SRE);
    }

    public static Book devOpsIsALie() {
        return new Book(AUTHOR_JANE, TITLE_DEVOPS);
    }
}
