package com.example.booksapi.tests;

import com.example.booksapi.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static com.example.booksapi.model.TestData.AUTHOR_JOHN;
import static com.example.booksapi.model.TestData.TITLE_SRE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Books API")
@Feature("Validation: read-only id")
public class ReadOnlyIdTest extends BaseTest {

    @DataProvider(name = "ids")
    public Object[][] ids() {
        return new Object[][]{
                {0},    // the id the server would assign next
                {42},   // an arbitrary id
        };
    }

    @Test(dataProvider = "ids", description = "Requirement 4: the id field is read-only")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Clients must not be able to send an id in PUT /api/books/. The server rejects it with 400.")
    public void idCannotBeSentByClient(int id) {
        Map<String, Object> body = Map.of("id", id, "author", AUTHOR_JOHN, "title", TITLE_SRE);

        Response response = api.createBook(body);

        assertThat("Status code when sending id " + id, response.statusCode(), equalTo(400));
        assertThat("Error message", response.jsonPath().getString("error"),
                equalTo("Field \"id\" is read-only"));
        assertThat("Nothing must be stored after a rejected request",
                api.parseBooks(api.listBooks()), is(empty()));
    }
}
