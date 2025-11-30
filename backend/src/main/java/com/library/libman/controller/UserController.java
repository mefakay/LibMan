package com.library.libman.controller;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.entity.BorrowRequest;
import com.library.libman.service.BookService;
import com.library.libman.service.BorrowService;
import com.library.libman.service.UserService;
import com.library.libman.service.BorrowRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize; // Yeni
import org.springframework.security.core.Authentication; // Yeni
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Kullanıcı işlemleri için API endpoint'leri
@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')") // USER veya ADMIN rolü gerektirir
public class UserController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowRequestService requestService;

    // Ödünç alınabilir kitapları getirir
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAvailableBooks() {
        List<Book> books = bookService.getAvailableBooks();
        return ResponseEntity.ok(books);
    }

    // Tüm kitapları getirir (ödünç alınmış olanlar dahil)
    @GetMapping("/books/all")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }

    /**
     * Oturum açmış kullanıcının kitap ödünç alması (ID'yi Authentication nesnesinden alır)
     */
    @PostMapping("/borrow/{bookId}")
    public ResponseEntity<?> borrowBook(@PathVariable Long bookId, Authentication authentication) {
        try {
            // Oturum açan kullanıcının adını/ID'sini al
            User user = userService.getUserByUsername(authentication.getName());

            Borrow borrow = borrowService.borrowBook(user.getId(), bookId);
            return ResponseEntity.status(HttpStatus.CREATED).body(borrow);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // Kitabı iade eder
    @PostMapping("/return/{borrowId}")
    public ResponseEntity<?> returnBook(@PathVariable @NonNull Long borrowId) {
        try {
            Borrow borrow = borrowService.returnBook(borrowId);
            return ResponseEntity.ok(borrow);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Oturum açmış kullanıcının tüm ödünç kayıtlarını getirir
     */
    @GetMapping("/borrows/me")
    public ResponseEntity<List<Borrow>> getUserBorrows(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<Borrow> borrows = borrowService.getUserBorrows(user.getId());
        return ResponseEntity.ok(borrows);
    }

    /**
     * Oturum açmış kullanıcının aktif ödünç kayıtlarını getirir
     */
    @GetMapping("/borrows/me/active")
    public ResponseEntity<List<Borrow>> getUserActiveBorrows(Authentication authentication) {
        User user = userService.getUserByUsername(authentication.getName());
        List<Borrow> borrows = borrowService.getUserActiveBorrows(user.getId());
        return ResponseEntity.ok(borrows);
    }

    //
    // Kitap ödünç isteği alır
    @PostMapping("/borrow-request/{username}/{bookId}")
    public ResponseEntity<?> requestBook(@PathVariable @NonNull String username, @PathVariable @NonNull Long bookId) {
        try {
            User user = userService.getUserByUsername(username);
            BorrowRequest request = requestService.borrowRequestBook(user.getId(), bookId);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    //Kullanıcının ödünç isteklerini getirir
    @GetMapping("/{username}/borrow-requests")
    public ResponseEntity<?> getUserRequestByUsername(@PathVariable @NonNull String username) {
        try {
            User user = userService.getUserByUsername(username);
            List<BorrowRequest> request = requestService.getUserBorrowRequests(user.getId());
            return ResponseEntity.ok(request);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // Not: Eski /borrow/username/{username}/{bookId} gibi metodlar güvenlik nedeniyle kaldırıldı veya güncellendi.
}