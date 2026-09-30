package com.example.booksapi.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

/**
 * A book as exchanged with the REST API (serialized to/from JSON).
 * The id is assigned by the server; null ids are omitted from the JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {

    private Integer id;
    private String author;
    private String title;

    /**
     * Required by Jackson for deserialization.
     */
    public Book() {
    }

    public Book(String author, String title) {
        this.author = author;
        this.title = title;
    }

    public Book(Integer id, String author, String title) {
        this.id = id;
        this.author = author;
        this.title = title;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Book other)) return false;
        return Objects.equals(id, other.id)
                && Objects.equals(author, other.author)
                && Objects.equals(title, other.title);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, author, title);
    }

    @Override
    public String toString() {
        return "Book{id=" + id + ", author='" + author + "', title='" + title + "'}";
    }
}
