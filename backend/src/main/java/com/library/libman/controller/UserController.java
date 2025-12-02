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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public class UserController {

    @Autowired private BookService bookService;
    @Autowired private BorrowService borrowService;
    @Autowired private UserService userService;
    @Autowired private BorrowRequestService requestService;
    @Autowired private ProfileUpdateRequestService profileRequestService;

    @GetMapping("/books/all")
    public ResponseEntity<List<Book>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/books/title/{title}")
    public ResponseEntity<List<Book>> getBooksByTitle(@PathVariable String title) {
        return ResponseEntity.ok(bookService.getBooksByTitle(title));
    }

    @PostMapping("/borrow-request/{username}/{bookId}")
    public ResponseEntity<?> requestBook(@PathVariable String username, @PathVariable Long bookId) {
        try {
            User user = userService.getUserByUsername(username);
            BorrowRequest request = requestService.borrowRequestBook(user.getId(), bookId);
            return ResponseEntity.status(HttpStatus.CREATED).body(request);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{username}/borrow-requests")
    public ResponseEntity<?> getUserRequestByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(requestService.getUserBorrowRequests(user.getId()));
    }

    @GetMapping("/borrows/username/{username}")
    public ResponseEntity<?> getUserBorrowsByUsername(@PathVariable String username) {
        User user = userService.getUserByUsername(username);
        return ResponseEntity.ok(borrowService.getUserBorrows(user.getId()));
    }

    @PostMapping("/return/username/{username}/{borrowId}")
    public ResponseEntity<?> returnBookByUsername(@PathVariable String username, @PathVariable Long borrowId) {
        try {
            return ResponseEntity.ok(borrowService.returnBook(borrowId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // PROFİL GÜNCELLEME İSTEĞİ
    @PostMapping("/settings/profile-request")
    public ResponseEntity<?> requestProfileUpdate(@RequestBody User tempUser, Authentication auth) {
        try {
            User currentUser = userService.getUserByUsername(auth.getName());
            profileRequestService.createRequest(currentUser.getId(), tempUser.getUsername(), tempUser.getEmail());
            return ResponseEntity.ok("İstek gönderildi.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // KENDİ BİLGİLERİNİ GETİR
    @GetMapping("/me")
    public ResponseEntity<User> getMe(Authentication auth) {
        User user = userService.getUserByUsername(auth.getName());
        user.setPassword(null);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/books/search")
    public ResponseEntity<List<Book>> searchBooks(@RequestParam(required = false) String title) {
        // Eğer arama kutusu boşsa tüm kitapları getir
        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.ok(bookService.getAllBooks());
        }
        // Doluysa isme göre getir (Mevcut metodun)
        return ResponseEntity.ok(bookService.getBooksByTitle(title));
    }
}