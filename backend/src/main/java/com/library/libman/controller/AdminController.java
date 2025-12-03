package com.library.libman.controller;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.entity.BorrowRequest;
import com.library.libman.service.BookService;
import com.library.libman.service.BorrowService;
import com.library.libman.service.UserService;
import com.library.libman.service.BorrowRequestService;
import com.library.libman.service.ProfileUpdateRequestService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private BookService bookService;
    @Autowired private UserService userService;
    @Autowired private BorrowService borrowService;
    @Autowired private BorrowRequestService requestService;
    @Autowired private ProfileUpdateRequestService profileRequestService;

    //  kitap işlemi
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @PostMapping("/books")
    public ResponseEntity<?> addBook(@Valid @RequestBody Book book) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(bookService.addBook(book));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok("Kitap silindi");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @RequestBody Book book) {
        try {
            return ResponseEntity.ok(bookService.updateBook(id, book));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // kullanıcı işlemi
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("Kullanıcı silindi.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ödünç istek
    @GetMapping("/borrows")
    public ResponseEntity<List<Borrow>> getAllBorrows() {
        return ResponseEntity.ok(borrowService.getAllBorrows());
    }

    @GetMapping("/borrow-requests/pending")
    public ResponseEntity<List<BorrowRequest>> getAllRequests() {
        return ResponseEntity.ok(requestService.getAllBorrowRequests());
    }

    @PostMapping("/borrow-requests/{requestId}/approve")
    public ResponseEntity<?> approveBorrowRequest(@PathVariable Long requestId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(requestService.acceptRequest(requestId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/borrow-requests/{requestId}/reject")
    public ResponseEntity<?> rejectBorrowRequest(@PathVariable Long requestId) {
        try {
            return ResponseEntity.ok(requestService.rejectRequest(requestId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //güncelle profili
    @GetMapping("/profile-requests/pending")
    public ResponseEntity<?> getProfileRequests() {
        return ResponseEntity.ok(profileRequestService.getPendingRequests());
    }

    @PostMapping("/profile-requests/{id}/approve")
    public ResponseEntity<?> approveProfileRequest(@PathVariable Long id) {
        try {
            profileRequestService.approveRequest(id);
            return ResponseEntity.ok("Onaylandı");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/profile-requests/{id}/reject")
    public ResponseEntity<?> rejectProfileRequest(@PathVariable Long id) {
        profileRequestService.rejectRequest(id);
        return ResponseEntity.ok("Reddedildi");
    }

    @GetMapping("/books/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam(required = false) String title) {
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.ok(bookService.getAllBooks());
        }
        return ResponseEntity.ok(bookService.getBooksByTitle(title));
    }
}