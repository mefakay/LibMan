package com.library.libman.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "books")
public class Book {
    
    //Book entity classının içeriği

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(nullable = false, unique = true)
    private String isbn;
    
    private Integer publicationYear;
    
    @Column(nullable = false)
    private Integer totalCopies;
    
    @Column(nullable = false)
    private Integer availableCopies;
    
    // Constructor'lar
    public Book() {
    }
    
    public Book(String title, String author, String isbn, Integer publicationYear, 
                Integer totalCopies, Integer availableCopies) {
        this.title = title;
        this.author = author;
        this.isbn = isbn;
        this.publicationYear = publicationYear;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }
    
    // Getter'lar
    public Long getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public Integer getPublicationYear() {
        return publicationYear;
    }
    
    public Integer getTotalCopies() {
        return totalCopies;
    }
    
    public Integer getAvailableCopies() {
        return availableCopies;
    }
    
    
    // Setter'lar
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }
    
    public void setTotalCopies(Integer totalCopies) {
        this.totalCopies = totalCopies;
    }
    
    public void setAvailableCopies(Integer availableCopies) {
        this.availableCopies = availableCopies;
    }
}
