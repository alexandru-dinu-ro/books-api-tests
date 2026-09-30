package com.example.booksapi.tests;

import com.example.booksapi.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Books API")
@Feature("Robustness: invalid requests")
public class InvalidRequestTest extends BaseTest {

    @DataProvider(name = "invalidBodies")
    public Object[][] invalidBodies() {
        return new Object[][]{
                {"malformed JSON", "{not json"},
                {"truncated JSON", "{\"author\": \"John Smith\", \"title\": \"SRE 101\""},
                {"trailing comma", "{\"author\": \"John Smith\", \"title\": \"SRE 101\",}"},
                {"plain text", "just some text"},
                {"JSON array instead of an object", "[]"},
                {"JSON string instead of an object", "\"text\""},
                {"JSON number instead of an object", "42"},
                {"JSON null instead of an object", "null"},
                {"author is a number", "{\"author\": 123, \"title\": \"SRE 101\"}"},
                {"title is a boolean", "{\"author\": \"John Smith\", \"title\": true}"},
                {"author is an object", "{\"author\": {\"name\": \"John\"}, \"title\": \"SRE 101\"}"},
        };
    }

    @Test(dataProvider = "invalidBodies",
            description = "Invalid request bodies are rejected with HTTP 400 and an error message")
    @Severity(SeverityLevel.NORMAL)
    @Description("Broken JSON, non-object JSON and fields of the wrong type must produce HTTP 400 with an "
            + "'error' message, never HTTP 500, and must not store anything.")
    public void invalidBodyIsRejected(String scenario, String rawBody) {
        Response response = api.createBookRaw(rawBody);

        assertThat("Status code (" + scenario + ")", response.statusCode(), equalTo(400));
        assertThat("Error message (" + scenario + ")",
                response.jsonPath().getString("error"), not(emptyOrNullString()));
        assertThat("Nothing must be stored after a rejected request",
                api.parseBooks(api.listBooks()), is(empty()));
    }

    @DataProvider(name = "nonNumericIds")
    public Object[][] nonNumericIds() {
        return new Object[][]{
                {"abc"},
                {"1.5"},
                {"99999999999"},   // too large for an int
        };
    }

    @Test(dataProvider = "nonNumericIds",
            description = "GET with a non-numeric book id is a client error, not a server error")
    @Severity(SeverityLevel.NORMAL)
    @Description("The spec does not define this case, so the test only requires a client error (400 or 404). "
            + "An HTTP 500 fails the test through the client's 5xx guard.")
    public void nonNumericIdIsNotAServerError(String id) {
        Response response = api.getBookByText(id);

        assertThat("Status code for id '" + id + "'",
                response.statusCode(), anyOf(equalTo(400), equalTo(404)));
    }
}
