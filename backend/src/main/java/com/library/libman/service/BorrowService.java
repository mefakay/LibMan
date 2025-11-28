package com.library.libman.service;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.BorrowRepository;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Ödünç alma ve iade işlemlerini yöneten servis
@Service
public class BorrowService {
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    
    // Kitap ödünç alır
    public Borrow borrowBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        

         Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + bookId));
        
        // Kitap mevcut mu kontrol et
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Kitap şu anda mevcut değil: " + book.getTitle());
        }
        
        // Kullanıcı bu kitabı zaten ödünç almış mı kontrol et
        Optional<Borrow> existingBorrow = borrowRepository.findByUserAndBookAndStatus(
                user, book, Borrow.BorrowStatus.ACTIVE);
        


        if (existingBorrow.isPresent()) {
            throw new RuntimeException("Bu kitabı zaten ödünç almışsınız: " + book.getTitle());
        }
        
        // Yeni ödünç kaydı oluştur
        Borrow borrow = new Borrow();
            borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDate.now());
        borrow.setStatus(Borrow.BorrowStatus.ACTIVE);
        
        // Kitabın mevcut kopya sayısını azalt
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        return borrowRepository.save(borrow);
    }
    
    // Kitabı iade eder
    public Borrow returnBook(Long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new RuntimeException("Ödünç kaydı bulunamadı: " + borrowId));
        

        if (borrow.getStatus() == Borrow.BorrowStatus.RETURNED) {
            throw new RuntimeException("Bu kitap zaten iade edilmiş");
        }
        
        // İade işlemini yap
        borrow.setReturnDate(LocalDate.now());
        borrow.setStatus(Borrow.BorrowStatus.RETURNED);
        
        // Kitabın mevcut kopya sayısını artır
        Book book = borrow.getBook();
        if (book.getAvailableCopies() < book.getTotalCopies()) {
                book.setAvailableCopies(book.getAvailableCopies() + 1);
        }
        bookRepository.save(book);
        
            return borrowRepository.save(borrow);
    }
    
    // Kullanıcının tüm ödünç kayıtlarını getirir
    public List<Borrow> getUserBorrows(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        
        return borrowRepository.findByUser(user);
    }
    

    // Kullanıcının aktif ödünç kayıtlarını getirir
    public List<Borrow> getUserActiveBorrows(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        

        return borrowRepository.findByUserAndStatus(user, Borrow.BorrowStatus.ACTIVE);
    }
    
    // Tüm ödünç kayıtlarını getirir
    public List<Borrow> getAllBorrows() {
        return borrowRepository.findAll();
    }
    

    
    // Kullanıcı adı ve kitap ID ile aktif ödünç kaydını bulur
    public Borrow getActiveBorrowByUsernameAndBook(String username, Long bookId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
        

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + bookId));
        
        return borrowRepository.findByUserAndBookAndStatus(user, book, Borrow.BorrowStatus.ACTIVE)
                .orElseThrow(() -> new RuntimeException("Aktif ödünç kaydı bulunamadı"));
    }
}

