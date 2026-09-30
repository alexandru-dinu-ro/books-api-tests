package com.example.booksapi.tests;

import com.example.booksapi.base.BaseTest;
import com.example.booksapi.model.Book;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

import static com.example.booksapi.model.TestData.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Books API")
@Feature("Validation: duplicate books")
public class DuplicateBookTest extends BaseTest {

    private static final String DUPLICATE_ERROR =
            "Another book with similar title and author already exists";

    @DataProvider(name = "duplicates")
    public Object[][] duplicates() {
        return new Object[][]{
                {"exact same title and author", new Book(AUTHOR_JOHN, TITLE_SRE)},
                {"different letter case", new Book("JOHN SMITH", "sre 101")},
                {"extra spaces around the values", new Book("  John Smith ", " SRE 101  ")},
        };
    }

    @Test(dataProvider = "duplicates", description = "Requirement 6: a duplicate book cannot be created")
    @Severity(SeverityLevel.CRITICAL)
    @Description("PUT /api/books/ with the same (or similar) title and author as an existing book returns 400 "
            + "and 'Another book with similar title and author already exists'.")
    public void duplicateBookIsRejected(String scenario, Book duplicate) {
        Book original = api.parseBook(api.createBook(sre101()));

        Response response = api.createBook(duplicate);

        assertThat("Status code (" + scenario + ")", response.statusCode(), equalTo(400));
        assertThat("Error message (" + scenario + ")",
                response.jsonPath().getString("error"), equalTo(DUPLICATE_ERROR));
        assertThat("Only the original book must remain",
                api.parseBooks(api.listBooks()), contains(original));
    }

    @DataProvider(name = "notDuplicates")
    public Object[][] notDuplicates() {
        return new Object[][]{
                {"same author, different title", new Book(AUTHOR_JOHN, TITLE_DEVOPS)},
                {"same title, different author", new Book(AUTHOR_JANE, TITLE_SRE)},
        };
    }

    @Test(dataProvider = "notDuplicates",
            description = "Books that differ in title or author are not duplicates")
    @Severity(SeverityLevel.NORMAL)
    @Description("Only a book with the same title AND author is a duplicate; changing either one makes it a new book.")
    public void differentBookIsAccepted(String scenario, Book different) {
        api.createBook(sre101());

        Response response = api.createBook(different);

        assertThat("Status code (" + scenario + ")", response.statusCode(), equalTo(201));
        List<Book> books = api.parseBooks(api.listBooks());
        assertThat("Both books must be stored", books, hasSize(2));
    }
}
