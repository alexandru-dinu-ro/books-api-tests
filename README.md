# Books API Tests

[![API Tests](https://github.com/alexandru-dinu-ro/books-api-tests/actions/workflows/tests.yml/badge.svg)](https://github.com/alexandru-dinu-ro/books-api-tests/actions/workflows/tests.yml)
[![Regression Detection](https://github.com/alexandru-dinu-ro/books-api-tests/actions/workflows/regression-check.yml/badge.svg)](https://github.com/alexandru-dinu-ro/books-api-tests/actions/workflows/regression-check.yml)

An automated test framework for a REST API that stores a list of books. It ships with a **stateful mock of the API**, so the whole suite runs anywhere with no external dependencies.

**Live Allure report (with run history): https://alexandru-dinu-ro.github.io/books-api-tests/**

## Tech stack

| Purpose | Tool |
|---|---|
| Language / build | Java 21, Maven (with Maven Wrapper) |
| Test runner | TestNG |
| HTTP client | RestAssured |
| Assertions | Hamcrest |
| Reporting | Allure 3 (published to GitHub Pages with trend history) |
| API mock | Javalin (embedded, stateful) |
| Logging | SLF4J + Logback |
| CI | GitHub Actions |

## The API under test

| Endpoint | Behavior |
|---|---|
| `GET /api/books/` | Returns the list of books |
| `PUT /api/books/` | Creates a book and returns it. Returns an error if anything is wrong |
| `GET /api/books/<book_id>/` | Returns one book, or HTTP 404 if it does not exist |

A book has a read-only `id`, an `author` and a `title`. Errors use HTTP 400 and the body `{"error": "<message>"}`. The API must never answer with HTTP 500.

## What is tested

| # | Requirement | Test class | Tests |
|---|---|---|---|
| 1 | The API starts with an empty store (plus 404 for unknown ids) | `EmptyStoreTest` | 2 |
| 2 | `title` and `author` are required | `RequiredFieldsTest` | 4 |
| 3 | `title` and `author` cannot be empty (including whitespace only) | `EmptyFieldsTest` | 4 |
| 4 | `id` is read-only | `ReadOnlyIdTest` | 2 |
| 5 | A book can be created via `PUT`, returned, and fetched again | `CreateBookTest` | 4 |
| 6 | A duplicate book cannot be created | `DuplicateBookTest` | 5 |
| - | Robustness: malformed JSON, non-object JSON, wrong field types, non-numeric ids (extra, beyond the six requirements) | `InvalidRequestTest` | 14 |
| - | Minimal suite: one plain-RestAssured test per requirement, with no wrapper classes (extra, repeats the requirements above) | `SimpleBooksApiTest` | 8 |

In total, 43 tests. Requests made through `ApiClient` (all classes except `SimpleBooksApiTest`) also pass through a guard that fails the test on any HTTP 5xx response. `SimpleBooksApiTest` asserts an exact status code in every test, so a 5xx fails it too.

## Proof that the tests catch regressions

The mock can deliberately break one rule at a time (`-Dmock.broken=<mode>`). A separate workflow runs the suite once per mode and **passes only if the expected tests fail**:

| Broken mode | Defect simulated | Test class that must fail |
|---|---|---|
| `duplicates` | Duplicate books are accepted | `DuplicateBookTest` |
| `empty-fields` | Empty values are accepted | `EmptyFieldsTest` |
| `writable-id` | The client can set the id | `ReadOnlyIdTest` |
| `server-error` | Creating a book returns HTTP 500 | `CreateBookTest` |

## Running locally

Requirements: JDK 21. Maven is not needed, because the Maven Wrapper downloads it.

```bash
./mvnw clean test
```

To try a broken mock:

```bash
./mvnw clean test -Dmock.broken=duplicates
```

To view the Allure report (requires Node.js 20 or newer):

```bash
npx allure generate target/allure-results --output target/allure-report
npx allure open target/allure-report
```

### Configuration

| Property | Default | Meaning |
|---|---|---|
| `mock.broken` | `none` | Deliberate defect: `duplicates`, `empty-fields`, `writable-id`, `server-error` |
| `mock.port` | `0` | Mock port (`0` picks any free port) |
| `api.root` | not set | Test a real API at this URL instead of starting the mock (also read from the `API_ROOT` environment variable). The API must provide the same reset endpoint as the mock |

## Project structure

```
src/test/java/com/example/booksapi/
  base/     BaseTest: starts the mock once, resets the data before every test
  client/   ApiClient: RestAssured wrapper with Allure steps and a 5xx guard
  config/   TestConfig: settings from system properties and environment variables
  mock/     MockBooksServer (Javalin), BookStore (business rules), BrokenMode
  model/    Book, TestData (fixed test data)
  tests/    One test class per requirement, plus robustness tests and a minimal plain-RestAssured suite
```

## Continuous integration

- **`API Tests`** runs on every push and pull request. On `main` it publishes the Allure report to GitHub Pages and keeps the trend history of the last 20 runs.
- **`Regression Detection`** runs the suite against each broken mock mode and verifies that the defect is detected.
- **Test logs:** every `API Tests` run also uploads a `test-logs` artifact with the full Logback log and the Surefire reports. It is uploaded even when tests fail, and kept for 14 days. Download it from the run page on the Actions tab.

## Design decisions and assumptions

The API description leaves some behavior open. These are the choices made, each covered by tests:

- A successful `PUT` returns **201 Created**.
- Ids are assigned by the server, starting at **0** and increasing by 1.
- Sending an `id` in `PUT` returns **400** with `Field "id" is read-only`, instead of silently ignoring it.
- "Similar" duplicates ignore letter case, leading and trailing spaces, and repeated spaces.
- Whitespace-only values count as empty.
- A missing or `null` field is reported as required, and an empty one as empty.
- The mock has a test-only endpoint, `POST /api/reset` (204), that clears all books before each test.
- Tests run sequentially, because they share one mock server.

## License

[MIT](LICENSE)
