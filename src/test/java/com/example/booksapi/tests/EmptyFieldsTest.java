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
@Feature("Validation: empty fields")
public class EmptyFieldsTest extends BaseTest {

    @DataProvider(name = "emptyFields")
    public Object[][] emptyFields() {
        return new Object[][]{
                {"author", "author is an empty string", Map.of("author", "", "title", TITLE_SRE)},
                {"title", "title is an empty string", Map.of("author", AUTHOR_JOHN, "title", "")},
                {"author", "author is whitespace only", Map.of("author", "   ", "title", TITLE_SRE)},
                {"title", "title is whitespace only", Map.of("author", AUTHOR_JOHN, "title", "   ")},
        };
    }

    @Test(dataProvider = "emptyFields",
            description = "Requirement 3: title and author cannot be empty")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /api/books/ with an empty or blank field returns 400 and 'Field \"<name>\" cannot be empty'.")
    public void emptyFieldIsRejected(String field, String scenario, Map<String, Object> body) {
        Response response = api.createBook(body);

        assertThat("Status code (" + scenario + ")", response.statusCode(), equalTo(400));
        assertThat("Error message (" + scenario + ")",
                response.jsonPath().getString("error"),
                equalTo("Field \"" + field + "\" cannot be empty"));
        assertThat("Nothing must be stored after a rejected request",
                api.parseBooks(api.listBooks()), is(empty()));
    }
}
