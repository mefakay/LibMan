package com.library.libman.controller;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.entity.BorrowRequest;
import com.library.libman.service.BookService;
import com.library.libman.service.BorrowService;
import com.library.libman.service.UserService;
import com.library.libman.service.BorrowRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize; // Yeni
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Yönetici işlemleri için API endpoint'leri
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')") // Sınıf düzeyinde ADMIN yetkilendirmesi
public class AdminController {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BorrowRequestService requestService;

    // Tüm kitapları getirir
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    // Yeni kitap ekler
    @PostMapping("/books")
    public ResponseEntity<?> addBook(@Valid @RequestBody Book book) {
        try {
            Book savedBook = bookService.addBook(book);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Kitabı siler
    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable @NonNull Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok("Kitap başarıyla silindi");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Kitap bilgilerini günceller
    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable @NonNull Long id, @Valid @RequestBody Book book) {
        try {
            Book updatedBook = bookService.updateBook(id, book);
            return ResponseEntity.ok(updatedBook);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // ID'ye göre kitap getirir
    @GetMapping("/books/{id}")
    public ResponseEntity<?> getBookById(@PathVariable @NonNull Long id) {
        return bookService.getBookById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Tüm kullanıcıları getirir
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Tüm ödünç kayıtlarını getirir
    @GetMapping("/borrows")
    public ResponseEntity<List<Borrow>> getAllBorrows() {
        List<Borrow> borrows = borrowService.getAllBorrows();
        return ResponseEntity.ok(borrows);
    }

    //
    // Tüm ödünç isteklerini getirir
    @GetMapping("/borrow-requests/pending")
    public ResponseEntity<List<BorrowRequest>> getAllRequests() {
        List<BorrowRequest> requests = requestService.getAllBorrowRequests();
        return ResponseEntity.ok(requests);
    }

    // Kitap ödünç isteğini onaylar
    @PostMapping("/borrow-requests/{requestId}/approve")
    public ResponseEntity<?> approveBorrowRequest(@PathVariable Long requestId) {
        try {
            Borrow borrow = requestService.acceptRequest(requestId);
            return ResponseEntity.status(HttpStatus.CREATED).body(borrow);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Talebi reddeder
    @PostMapping("/borrow-requests/{requestId}/reject")
    public ResponseEntity<?> rejectBorrowRequest(
            @PathVariable Long requestId) {
        try {
            BorrowRequest request = requestService.rejectRequest(requestId);
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}