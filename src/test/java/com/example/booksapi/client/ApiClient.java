package com.example.booksapi.client;

import com.example.booksapi.model.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Step;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.restassured.RestAssured.given;

/**
 * Thin wrapper around the Books REST API.
 * Every call is logged, attached to the Allure report, and fails the test on HTTP 5xx.
 */
public class ApiClient {

    private static final Logger log = LoggerFactory.getLogger(ApiClient.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Logs each call and fails the test if the server answers with an HTTP 5xx (never intended by the API).
     */
    private static final Filter SERVER_ERROR_GUARD = (request, responseSpec, ctx) -> {
        Response response = ctx.next(request, responseSpec);
        log.info("{} {} -> {} ({} ms)",
                request.getMethod(), request.getURI(), response.getStatusCode(), response.getTime());
        if (response.getStatusCode() >= 500) {
            throw new AssertionError("Server error: HTTP " + response.getStatusCode()
                    + " for " + request.getMethod() + " " + request.getURI()
                    + ", body: " + response.asString());
        }
        return response;
    };

    private final RequestSpecification spec;

    public ApiClient(String baseUrl) {
        this.spec = new RequestSpecBuilder()
                .setBaseUri(baseUrl)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(SERVER_ERROR_GUARD)        // outermost: runs after Allure has recorded the exchange
                .addFilter(new AllureRestAssured())   // attaches request and response to the report
                .build();
    }

    @Step("GET /api/books/")
    public Response listBooks() {
        return given().spec(spec).get("/api/books/");
    }

    @Step("GET /api/books/{id}/")
    public Response getBook(int id) {
        return given().spec(spec).get("/api/books/{id}/", id);
    }

    /**
     * Sends PUT /api/books/ with the given body (a Book, a Map, ...) serialized to JSON.
     */
    @Step("PUT /api/books/")
    public Response createBook(Object body) {
        return given().spec(spec).body(toJson(body)).put("/api/books/");
    }

    /**
     * Test helper exposed by the mock only: removes all books.
     */
    @Step("POST /api/reset")
    public Response reset() {
        return given().spec(spec).post("/api/reset");
    }

    /** Sends PUT /api/books/ with a raw text body exactly as given (used to send invalid JSON). */
    @Step("PUT /api/books/ (raw body)")
    public Response createBookRaw(String rawBody) {
        return given().spec(spec).body(rawBody).put("/api/books/");
    }

    /** Sends GET /api/books/{id}/ with an id that is not necessarily a number. */
    @Step("GET /api/books/{id}/ (id as text)")
    public Response getBookByText(String id) {
        return given().spec(spec).get("/api/books/{id}/", id);
    }

    // ---------- response parsing ----------

    public Book parseBook(Response response) {
        try {
            return MAPPER.readValue(response.asString(), Book.class);
        } catch (JsonProcessingException e) {
            throw new AssertionError("Response is not a valid book: " + response.asString(), e);
        }
    }

    public List<Book> parseBooks(Response response) {
        try {
            return MAPPER.readValue(response.asString(), new TypeReference<List<Book>>() {
            });
        } catch (JsonProcessingException e) {
            throw new AssertionError("Response is not a valid list of books: " + response.asString(), e);
        }
    }

    private static String toJson(Object body) {
        try {
            return MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Cannot serialize request body", e);
        }
    }
}
