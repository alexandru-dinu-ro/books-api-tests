package com.example.booksapi.mock;

import com.example.booksapi.model.Book;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * In-memory book storage with all the business rules of the API:
 * required fields, non-empty values, read-only id, and duplicate detection.
 * Rules can be deliberately broken via {@link BrokenMode} to prove the tests catch regressions.
 */
public class BookStore {

    /**
     * An error the API reports to the client with a specific HTTP status.
     */
    public static class ApiException extends RuntimeException {
        private final int status;

        public ApiException(int status, String message) {
            super(message);
            this.status = status;
        }

        public int status() {
            return status;
        }
    }

    private final BrokenMode brokenMode;
    private final List<Book> books = new ArrayList<>();
    private int nextId = 0;

    public BookStore(BrokenMode brokenMode) {
        this.brokenMode = brokenMode;
    }

    /**
     * Removes all books and restarts id numbering from 0.
     */
    public synchronized void reset() {
        books.clear();
        nextId = 0;
    }

    public synchronized List<Book> findAll() {
        return List.copyOf(books);
    }

    public synchronized Book findById(int id) {
        return books.stream()
                .filter(book -> book.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ApiException(404, "Book with id " + id + " not found"));
    }

    /**
     * Validates the request body and stores a new book.
     */
    public synchronized Book create(JsonNode body) {
        if (brokenMode == BrokenMode.SERVER_ERROR) {
            throw new IllegalStateException("Simulated internal server error");
        }
        if (body == null || !body.isObject()) {
            throw new ApiException(400, "Request body must be a JSON object");
        }

        // The id is assigned by the server, so clients must not send it.
        boolean idProvided = body.has("id");
        if (idProvided && brokenMode != BrokenMode.WRITABLE_ID) {
            throw new ApiException(400, "Field \"id\" is read-only");
        }

        // First check that both fields exist, then that neither is empty.
        String author = readRequiredString(body, "author");
        String title = readRequiredString(body, "title");
        if (brokenMode != BrokenMode.EMPTY_FIELDS) {
            requireNotBlank("author", author);
            requireNotBlank("title", title);
        }

        if (brokenMode != BrokenMode.DUPLICATES && isDuplicate(author, title)) {
            throw new ApiException(400, "Another book with similar title and author already exists");
        }

        int id = idProvided ? body.get("id").asInt() : nextId++;
        Book book = new Book(id, author, title);
        books.add(book);
        return book;
    }

    private String readRequiredString(JsonNode body, String field) {
        JsonNode node = body.get(field);
        if (node == null || node.isNull()) {
            throw new ApiException(400, "Field \"" + field + "\" is required");
        }
        if (!node.isTextual()) {
            throw new ApiException(400, "Field \"" + field + "\" must be a string");
        }
        return node.asText();
    }

    private void requireNotBlank(String field, String value) {
        if (value.isBlank()) {
            throw new ApiException(400, "Field \"" + field + "\" cannot be empty");
        }
    }

    /**
     * "Similar" means equal after trimming, collapsing spaces and ignoring case.
     */
    private boolean isDuplicate(String author, String title) {
        String normalizedAuthor = normalize(author);
        String normalizedTitle = normalize(title);
        return books.stream().anyMatch(book ->
                normalize(book.getAuthor()).equals(normalizedAuthor)
                        && normalize(book.getTitle()).equals(normalizedTitle));
    }

    private static String normalize(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
