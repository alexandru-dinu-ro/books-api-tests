package com.example.booksapi.tests;

import com.example.booksapi.base.BaseTest;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Books API")
@Feature("Initial state")
public class EmptyStoreTest extends BaseTest {

    @Test(description = "Requirement 1: the API starts with an empty store")
    @Severity(SeverityLevel.CRITICAL)
    @Description("At the beginning of a test case there should be no books stored on the server.")
    public void listIsEmptyAtStart() {
        Response response = api.listBooks();

        assertThat("Status code", response.statusCode(), equalTo(200));
        assertThat("Books on the server", api.parseBooks(response), is(empty()));
    }

    @Test(description = "GET a book by id returns 404 when it does not exist")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /api/books/<book_id>/ returns HTTP 404 Not Found if the book with the given id does not exist.")
    public void unknownBookIdReturnsNotFound() {
        assertThat("Status code for id 0", api.getBook(0).statusCode(), equalTo(404));
        assertThat("Status code for id 999", api.getBook(999).statusCode(), equalTo(404));
    }
}
