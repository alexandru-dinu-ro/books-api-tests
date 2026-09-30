package com.example.booksapi.mock;

import com.example.booksapi.model.Book;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Stateful mock of the Books REST API, built on an embedded Javalin server.
 * <p>
 * Endpoints:
 * GET  /api/books/         list all books
 * PUT  /api/books/         create a book (201)
 * GET  /api/books/{id}/    get one book (404 if missing)
 * POST /api/reset          test helper: removes all books (204)
 */
public class MockBooksServer {

    private static final Logger log = LoggerFactory.getLogger(MockBooksServer.class);
    private static final String JSON = "application/json";

    private final ObjectMapper mapper = new ObjectMapper();
    private final BrokenMode brokenMode;
    private final BookStore store;
    private Javalin app;

    public MockBooksServer(BrokenMode brokenMode) {
        this.brokenMode = brokenMode;
        this.store = new BookStore(brokenMode);
    }

    /**
     * Starts the server. Port 0 means "pick any free port".
     */
    public void start(int port) {
        app = Javalin.create(config -> {
            config.routes.get("/api/books/", safe(this::listBooks));
            config.routes.put("/api/books/", safe(this::createBook));
            config.routes.get("/api/books/{id}/", safe(this::getBook));
            config.routes.post("/api/reset", safe(this::reset));
        }).start(port);
        log.info("Mock API started at {} (broken mode: {})", baseUrl(), brokenMode.value());
    }

    public void stop() {
        if (app != null) {
            app.stop();
            log.info("Mock API stopped");
        }
    }

    public int port() {
        return app.port();
    }

    public String baseUrl() {
        return "http://localhost:" + port();
    }

    // ---------- endpoint handlers ----------

    private void listBooks(Context ctx) throws JsonProcessingException {
        sendJson(ctx, 200, store.findAll());
    }

    private void createBook(Context ctx) throws JsonProcessingException {
        JsonNode body;
        try {
            body = mapper.readTree(ctx.body());
        } catch (JsonProcessingException e) {
            throw new BookStore.ApiException(400, "Request body is not valid JSON");
        }
        Book created = store.create(body);
        log.info("Created {}", created);
        sendJson(ctx, 201, created);
    }

    private void getBook(Context ctx) throws JsonProcessingException {
        int id;
        try {
            id = Integer.parseInt(ctx.pathParam("id"));
        } catch (NumberFormatException e) {
            throw new BookStore.ApiException(400, "Book id must be a number");
        }
        sendJson(ctx, 200, store.findById(id));
    }

    private void reset(Context ctx) {
        store.reset();
        log.info("Store reset");
        ctx.status(204);
    }

    // ---------- helpers ----------

    /**
     * Turns exceptions into the API's error format: 400 with {"error": "..."}, or 500 for unexpected bugs.
     */
    private Handler safe(Handler handler) {
        return ctx -> {
            try {
                handler.handle(ctx);
            } catch (BookStore.ApiException e) {
                log.info("Responding {}: {}", e.status(), e.getMessage());
                sendError(ctx, e.status(), e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error", e);
                sendError(ctx, 500, "Internal server error");
            }
        };
    }

    private void sendJson(Context ctx, int status, Object payload) throws JsonProcessingException {
        ctx.status(status).contentType(JSON).result(mapper.writeValueAsString(payload));
    }

    private void sendError(Context ctx, int status, String message) {
        try {
            sendJson(ctx, status, Map.of("error", message));
        } catch (JsonProcessingException e) {
            ctx.status(500).result("Internal server error");
        }
    }
}
