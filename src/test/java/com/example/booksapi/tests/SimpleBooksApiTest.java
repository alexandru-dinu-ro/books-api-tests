package com.example.booksapi.tests;

import com.example.booksapi.base.BaseTest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * The minimum set of tests, one per requirement, using plain RestAssured
 * (no client wrapper, no model classes).
 * The mock is started and reset by BaseTest.
 */
@Epic("Books API")
@Feature("Minimal suite (plain RestAssured)")
public class SimpleBooksApiTest extends BaseTest {

    private static String apiPath() {
        return apiRoot() + "/api/books/";
    }

    /**
     * JSON request that is also recorded in the Allure report.
     */
    private static RequestSpecification request() {
        return RestAssured.given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON);
    }

    // Requirement 1
    @Test(description = "Requirement 1: the API starts with an empty store")
    public void verifyEmptyStoreAtSpinUp() {
        request()
                .get(apiPath())
                .then()
                .statusCode(200)
                .body("size()", is(0));
    }

    // Requirement 2
    @Test(description = "Requirement 2: author is required")
    public void verifyAuthorIsRequired() {
        request()
                .body("{\"title\": \"SRE 101\"}")
                .put(apiPath())
                .then()
                .statusCode(400)
                .body("error", equalTo("Field \"author\" is required"));
    }

    @Test(description = "Requirement 2: title is required")
    public void verifyTitleIsRequired() {
        request()
                .body("{\"author\": \"John Smith\"}")
                .put(apiPath())
                .then()
                .statusCode(400)
                .body("error", equalTo("Field \"title\" is required"));
    }

    // Requirement 3
    @Test(description = "Requirement 3: author cannot be empty")
    public void verifyAuthorCannotBeEmpty() {
        request()
                .body("{\"author\": \"\", \"title\": \"SRE 101\"}")
                .put(apiPath())
                .then()
                .statusCode(400)
                .body("error", equalTo("Field \"author\" cannot be empty"));
    }

    @Test(description = "Requirement 3: title cannot be empty")
    public void verifyTitleCannotBeEmpty() {
        request()
                .body("{\"author\": \"John Smith\", \"title\": \"\"}")
                .put(apiPath())
                .then()
                .statusCode(400)
                .body("error", equalTo("Field \"title\" cannot be empty"));
    }

    // Requirement 4
    @Test(description = "Requirement 4: the id field is read-only")
    public void verifyIdIsReadOnly() {
        request()
                .body("{\"id\": 5, \"author\": \"John Smith\", \"title\": \"SRE 101\"}")
                .put(apiPath())
                .then()
                .statusCode(400)
                .body("error", equalTo("Field \"id\" is read-only"));
    }

    // Requirement 5
    @Test(description = "Requirement 5: a book can be created and fetched again")
    public void verifyBookCanBeCreated() {
        int id = request()
                .body("{\"author\": \"John Smith\", \"title\": \"SRE 101\"}")
                .put(apiPath())
                .then()
                .statusCode(201)
                .body("author", equalTo("John Smith"))
                .body("title", equalTo("SRE 101"))
                .extract().path("id");

        request()
                .get(apiPath() + id + "/")
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("author", equalTo("John Smith"))
                .body("title", equalTo("SRE 101"));
    }

    // Requirement 6
    @Test(description = "Requirement 6: a duplicate book cannot be created")
    public void verifyDuplicateBookIsRejected() {
        String book = "{\"author\": \"John Smith\", \"title\": \"SRE 101\"}";

        request().body(book).put(apiPath()).then().statusCode(201);

        request()
                .body(book)
                .put(apiPath())
                .then()
                .statusCode(400)
                .body("error", equalTo("Another book with similar title and author already exists"));
    }
}
