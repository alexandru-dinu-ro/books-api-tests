package com.example.booksapi.config;

import java.util.Optional;

/**
 * Central place for framework settings.
 * Each setting can be overridden with a JVM system property (-Dname=value) or an environment variable.
 */
public final class TestConfig {

    /**
     * If set, tests run against this URL and the built-in mock is NOT started. Example: http://localhost:8080
     */
    private static final String API_ROOT_PROPERTY = "api.root";
    private static final String API_ROOT_ENV = "API_ROOT";

    /**
     * Port for the built-in mock. 0 means "pick any free port".
     */
    private static final String MOCK_PORT_PROPERTY = "mock.port";

    /**
     * Deliberately breaks one rule of the mock, to prove the tests catch regressions.
     */
    private static final String MOCK_BROKEN_PROPERTY = "mock.broken";

    private TestConfig() {
        // utility class, no instances
    }

    /**
     * The external API URL, if one was provided.
     */
    public static Optional<String> externalApiRoot() {
        String value = System.getProperty(API_ROOT_PROPERTY, System.getenv(API_ROOT_ENV));
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(value.replaceAll("/+$", "")); // strip trailing slashes
    }

    public static int mockPort() {
        return Integer.parseInt(System.getProperty(MOCK_PORT_PROPERTY, "0"));
    }

    /**
     * Name of the broken mode, e.g. "duplicates". Defaults to "none" (a correct mock).
     */
    public static String mockBrokenMode() {
        return System.getProperty(MOCK_BROKEN_PROPERTY, "none");
    }
}
