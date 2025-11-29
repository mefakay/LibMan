package com.library.libman.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "books")
public class Book {
    
    //Book entity classının içeriği

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Kitap başlığı boş olamaz")
    @Size(max = 100, message = "Başlık en fazla 100 karakter olabilir")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Yazar adı boş olamaz")
    @Size(max = 100, message = "Yazar adı en fazla 100 karakter olabilir")
    @Column(nullable = false)
    private String author;
    
    @NotBlank(message = "ISBN numarası boş olamaz")
    @Pattern(regexp = "^[0-9]{3}-[0-9]{3}-[0-9]{2}-[0-9]{4}-[0-9X]$", 
             message = "ISBN formatı geçersiz. Format: XXX-XXX-XX-XXXX-X (örn: 978-123-45-6789-0)")
    @Column(nullable = false, unique = true)
    private String isbn;
    
    @Min(value = 0, message = "Yayın yılı 0'dan küçük olamaz")
    @Max(value = 2026, message = "Yayın yılı 2026'dan büyük olamaz")
    private Integer publicationYear;
    
    @NotNull(message = "Toplam kopya sayısı belirtilmelidir")
    @Min(value = 1, message = "Toplam kopya sayısı en az 1 olmalı")
    @Column(nullable = false)
    private Integer totalCopies;
    
    @NotNull(message = "Mevcut kopya sayısı belirtilmelidir")
    @Min(value = 0, message = "Mevcut kopya sayısı negatif olamaz")
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
