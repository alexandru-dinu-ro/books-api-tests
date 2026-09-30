package com.example.booksapi.tests;

import com.example.booksapi.base.BaseTest;
import com.example.booksapi.model.Book;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static com.example.booksapi.model.TestData.devOpsIsALie;
import static com.example.booksapi.model.TestData.sre101;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Books API")
@Feature("Creating books")
public class CreateBookTest extends BaseTest {

    @Test(description = "Requirement 5: a created book is returned in the response")
    @Severity(SeverityLevel.BLOCKER)
    @Description("PUT /api/books/ creates the book (201) and returns it with a server-assigned id.")
    public void createdBookIsReturnedInResponse() {
        Book toCreate = sre101();

        Response response = api.createBook(toCreate);

        assertThat("Status code", response.statusCode(), equalTo(201));
        Book created = api.parseBook(response);
        assertThat("Server must assign an id", created.getId(), notNullValue());
        assertThat("Author", created.getAuthor(), equalTo(toCreate.getAuthor()));
        assertThat("Title", created.getTitle(), equalTo(toCreate.getTitle()));
    }

    @Test(description = "Requirement 5: GET by id returns the same book that was created")
    @Severity(SeverityLevel.BLOCKER)
    @Description("GET /api/books/<book_id>/ returns exactly the book returned by PUT.")
    public void createdBookCanBeFetchedById() {
        Book created = api.parseBook(api.createBook(sre101()));

        Response response = api.getBook(created.getId());

        assertThat("Status code", response.statusCode(), equalTo(200));
        assertThat("Fetched book", api.parseBook(response), equalTo(created));
    }

    @Test(description = "A created book appears in the list of books")
    @Severity(SeverityLevel.NORMAL)
    @Description("GET /api/books/ returns every created book.")
    public void createdBookAppearsInList() {
        Book created = api.parseBook(api.createBook(sre101()));

        List<Book> books = api.parseBooks(api.listBooks());

        assertThat("Books on the server", books, contains(created));
    }

    @Test(description = "Ids are assigned automatically, starting at 0 and increasing")
    @Severity(SeverityLevel.NORMAL)
    @Description("The first book gets id 0 and the second gets id 1; both are listed in creation order.")
    public void idsAreAssignedSequentiallyFromZero() {
        Book first = api.parseBook(api.createBook(sre101()));
        Book second = api.parseBook(api.createBook(devOpsIsALie()));

        assertThat("Id of the first book", first.getId(), equalTo(0));
        assertThat("Id of the second book", second.getId(), equalTo(1));
        assertThat("Books on the server", api.parseBooks(api.listBooks()), contains(first, second));
    }
}
