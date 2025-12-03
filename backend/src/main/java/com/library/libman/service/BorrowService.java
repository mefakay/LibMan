package com.library.libman.service;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.BorrowRepository;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BorrowService {
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    
    // ödünç al
    public Borrow borrowBook(@NonNull Long userId, @NonNull Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        

         Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + bookId));
        
        //  mevcut mu
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Kitap şu anda mevcut değil: " + book.getTitle());
        }
        
        // almış mı kontrol
        Optional<Borrow> existingBorrow = borrowRepository.findByUserAndBookAndStatus(
                user, book, Borrow.BorrowStatus.ACTIVE);
        


        if (existingBorrow.isPresent()) {
            throw new RuntimeException("Bu kitabı zaten ödünç almışsınız: " + book.getTitle());
        }
        
        // ödünç
        Borrow borrow = new Borrow();
            borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setStatus(Borrow.BorrowStatus.ACTIVE);
        
        // kopya azalt
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        return borrowRepository.save(borrow);
    }
    
    // iade
    public Borrow returnBook(@NonNull Long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Ödünç kaydı bulunamadı: " + borrowId));
        

        if (borrow.getStatus() == Borrow.BorrowStatus.RETURNED) {
            throw new RuntimeException("Bu kitap zaten iade edilmiş");
        }

        borrow.setReturnDate(LocalDate.now());
        borrow.setStatus(Borrow.BorrowStatus.RETURNED);
        
        // kopya artır
        Book book = borrow.getBook();
        if (book.getAvailableCopies() < book.getTotalCopies()) {
                book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        bookRepository.save(book);
        
            return borrowRepository.save(borrow);
    }
    
    // ödünç kayıtları
    public List<Borrow> getUserBorrows(@NonNull Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        
        return borrowRepository.findByUser(user);
    }
    

    // aktif ödünç
    public List<Borrow> getUserActiveBorrows(@NonNull Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        

        return borrowRepository.findByUserAndStatus(user, Borrow.BorrowStatus.ACTIVE);
    }
    
    // hepsi
    public List<Borrow> getAllBorrows() {
        return borrowRepository.findAll();
    }
    


    // ödünç kaydını bulur
    public Borrow getActiveBorrowByUsernameAndBook(@NonNull String username, @NonNull Long bookId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
        

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + bookId));
        
        return borrowRepository.findByUserAndBookAndStatus(user, book, Borrow.BorrowStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aktif ödünç kaydı bulunamadı"));
    }
}

