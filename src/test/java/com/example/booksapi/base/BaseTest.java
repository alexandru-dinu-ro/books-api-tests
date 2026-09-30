package com.example.booksapi.base;

import com.example.booksapi.client.ApiClient;
import com.example.booksapi.config.TestConfig;
import com.example.booksapi.mock.BrokenMode;
import com.example.booksapi.mock.MockBooksServer;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Base class for all API tests.
 * - Before the suite: starts the built-in mock (unless -Dapi.root points to a real API).
 * - Before each test: creates a fresh client and resets the server to an empty store.
 * - After the suite: stops the mock.
 */
public abstract class BaseTest {

    private static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    private static MockBooksServer mockServer;
    private static String apiRoot;

    /**
     * Client used by the tests. A new instance is created before every test.
     */
    protected ApiClient api;

    /** Root URL of the API under test (the built-in mock or the external API). */
    protected static String apiRoot() {
        return apiRoot;
    }

    @BeforeSuite(alwaysRun = true)
    public void startApi() {
        Optional<String> external = TestConfig.externalApiRoot();
        if (external.isPresent()) {
            apiRoot = external.get();
            log.info("Using external API at {}", apiRoot);
            return;
        }
        BrokenMode mode = BrokenMode.fromValue(TestConfig.mockBrokenMode());
        mockServer = new MockBooksServer(mode);
        mockServer.start(TestConfig.mockPort());
        apiRoot = mockServer.baseUrl();
    }

    @AfterSuite(alwaysRun = true)
    public void stopApi() {
        if (mockServer != null) {
            mockServer.stop();
            mockServer = null;
        }
    }

    @BeforeMethod(alwaysRun = true)
    public void resetApi() {
        api = new ApiClient(apiRoot);
        Response response = api.reset();
        assertThat("Reset endpoint should answer 204", response.statusCode(), equalTo(204));
    }
}
