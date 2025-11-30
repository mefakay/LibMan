package com.library.libman.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "borrowRequests")
public class BorrowRequest {
    
    //BorrowRequest entity classının içeriği

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
    private LocalDate requestDate;
    
    private LocalDate processedDate;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RequestStatus status;
    
    public enum RequestStatus {
        PENDING ,     
        APPROVED ,
        REJECTED     
    }
    

    
   
    public BorrowRequest() {
    }
    
    public BorrowRequest(User user, Book book, LocalDate requestDate, LocalDate processedDate, RequestStatus status) {
        this.user = user;
        this.book = book;
        this.requestDate = requestDate;
        this.processedDate = processedDate;
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
    
    public LocalDate getRequestDate() {
        return requestDate;
    }
    
    public LocalDate getProcessedDate() {
        return processedDate;
    }
    
    public RequestStatus getStatus() {
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
    
    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }
    
    public void setProcessedDate(LocalDate processedDate) {
        this.processedDate = processedDate;
    }
    
    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
