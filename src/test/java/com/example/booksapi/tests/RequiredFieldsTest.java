package com.example.booksapi.tests;

import com.example.booksapi.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static com.example.booksapi.model.TestData.AUTHOR_JOHN;
import static com.example.booksapi.model.TestData.TITLE_SRE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Books API")
@Feature("Validation: required fields")
public class RequiredFieldsTest extends BaseTest {

    @DataProvider(name = "missingFields")
    public Object[][] missingFields() {
        Map<String, Object> nullAuthor = new HashMap<>();
        nullAuthor.put("author", null);
        nullAuthor.put("title", TITLE_SRE);

        Map<String, Object> nullTitle = new HashMap<>();
        nullTitle.put("author", AUTHOR_JOHN);
        nullTitle.put("title", null);

        return new Object[][]{
                {"author", "author is missing", Map.of("title", TITLE_SRE)},
                {"title", "title is missing", Map.of("author", AUTHOR_JOHN)},
                {"author", "author is null", nullAuthor},
                {"title", "title is null", nullTitle},
        };
    }

    @Test(dataProvider = "missingFields",
            description = "Requirement 2: title and author are required fields")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /api/books/ without a required field returns 400 and 'Field \"<name>\" is required'.")
    public void missingFieldIsRejected(String field, String scenario, Map<String, Object> body) {
        Response response = api.createBook(body);

        assertThat("Status code (" + scenario + ")", response.statusCode(), equalTo(400));
        assertThat("Error message (" + scenario + ")",
                response.jsonPath().getString("error"),
                equalTo("Field \"" + field + "\" is required"));
        assertThat("Nothing must be stored after a rejected request",
                api.parseBooks(api.listBooks()), is(empty()));
    }
}
