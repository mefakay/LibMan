package com.library.libman.controller;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.service.BookService;
import com.library.libman.service.BorrowService;
import com.library.libman.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Kullanıcı işlemleri için API endpoint'leri
@RestController
@RequestMapping("/api/user")
public class UserController {
    
    @Autowired
    private BookService bookService;
    
    @Autowired
    private BorrowService borrowService;
    
    @Autowired
    private UserService userService;
    
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

    // Başlığa göre kitap arama (büyük/küçük harf duyarlılığı yok)
    @GetMapping("/books/title/{title}")
    public ResponseEntity<List<Book>> getBooksByTitle(@PathVariable @NonNull String title) {
        List<Book> books = bookService.getBooksByTitle(title);
        return ResponseEntity.ok(books);
    }

    
    // Kitap ödünç alır
    @PostMapping("/borrow/{userId}/{bookId}")
    public ResponseEntity<?> borrowBook(@PathVariable @NonNull Long userId, @PathVariable @NonNull Long bookId) {
        try {
            Borrow borrow = borrowService.borrowBook(userId, bookId);
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
    
    // Kullanıcının tüm ödünç kayıtlarını getirir
    @GetMapping("/borrows/{userId}")
    public ResponseEntity<List<Borrow>> getUserBorrows(@PathVariable @NonNull Long userId) {
        List<Borrow> borrows = borrowService.getUserBorrows(userId);
        return ResponseEntity.ok(borrows);
    }
    
    // Kullanıcının aktif ödünç kayıtlarını getirir
    @GetMapping("/borrows/{userId}/active")
    public ResponseEntity<List<Borrow>> getUserActiveBorrows(@PathVariable @NonNull Long userId) {
        List<Borrow> borrows = borrowService.getUserActiveBorrows(userId);
        return ResponseEntity.ok(borrows);
    }
    
    // Kullanıcı adı ile kitap ödünç alır
    @PostMapping("/borrow/username/{username}/{bookId}")
    public ResponseEntity<?> borrowBookByUsername(@PathVariable @NonNull String username, @PathVariable @NonNull Long bookId) {
        try {
            User user = userService.getUserByUsername(username);
            Borrow borrow = borrowService.borrowBook(user.getId(), bookId);
            return ResponseEntity.status(HttpStatus.CREATED).body(borrow);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    // Kullanıcı adı ile ödünç kayıtlarını getirir
    @GetMapping("/borrows/username/{username}")
    public ResponseEntity<?> getUserBorrowsByUsername(@PathVariable @NonNull String username) {
        try {
            User user = userService.getUserByUsername(username);
            List<Borrow> borrows = borrowService.getUserBorrows(user.getId());
            return ResponseEntity.ok(borrows);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    
    // Kullanıcı adı ile aktif ödünç kayıtlarını getirir
    @GetMapping("/borrows/username/{username}/active")
    public ResponseEntity<?> getUserActiveBorrowsByUsername(@PathVariable @NonNull String username) {
        try {
            User user = userService.getUserByUsername(username);
            List<Borrow> borrows = borrowService.getUserActiveBorrows(user.getId());
            return ResponseEntity.ok(borrows);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
    
    // Kullanıcı adı ve ödünç id'si ile kitap iade eder
    @PostMapping("/return/username/{username}/{borrowId}")
    public ResponseEntity<?> returnBookByUsername(
            @PathVariable @NonNull String username,
            @PathVariable @NonNull Long borrowId) {

        try {
            // 1) Ödünç kaydını getir (mevcut metot)
            Borrow borrow = borrowService.returnBook(borrowId);

            // 2) İade edilen kayıt gerçekten bu kullanıcıya mı ait kontrol edelim
            if (!borrow.getUser().getUsername().equals(username)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Bu ödünç kaydı belirtilen kullanıcıya ait değil!");
            }

            return ResponseEntity.ok(borrow);

        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}


