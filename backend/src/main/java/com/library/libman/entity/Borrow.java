package com.library.libman.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "borrows")
public class Borrow {
    
    //Borrow entity classının içeriği

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;
    
    @Column(nullable = false)
    private LocalDate borrowDate;
    
    private LocalDate returnDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BorrowStatus status;
    
    public enum BorrowStatus {
        ACTIVE,     
        RETURNED    
    }
    

    
   
    public Borrow() {
    }
    
    public Borrow(User user, Book book, LocalDate borrowDate, LocalDate returnDate, BorrowStatus status) {
        this.user = user;
        this.book = book;
        this.borrowDate = borrowDate;
        this.returnDate = returnDate;
        this.status = status;
    }
    
 
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public Book getBook() {
        return book;
    }
    
    public LocalDate getBorrowDate() {
        return borrowDate;
    }
    
    public LocalDate getReturnDate() {
        return returnDate;
    }
    
    public BorrowStatus getStatus() {
        return status;
    }
    
  
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setBook(Book book) {
        this.book = book;
    }
    
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }
    
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }
    
    public void setStatus(BorrowStatus status) {
        this.status = status;
    }
}
