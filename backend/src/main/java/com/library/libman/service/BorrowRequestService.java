package com.library.libman.service;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.BorrowRequest;
import com.library.libman.entity.User;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.BorrowRequestRepository;
import com.library.libman.repository.BorrowRepository;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Ödünç alma requestlerini yöneten servis
@Service
public class BorrowRequestService {
    
    @Autowired
    private BorrowRequestRepository borrowRequestRepository;
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
     @Autowired
    private BorrowRepository borrowRepository;
    
    // Kitap ödünç isteği alır
    public BorrowRequest borrowRequestBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        

         Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + bookId));
        
        // Kitap mevcut mu kontrol et
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Kitap şu anda mevcut değil: " + book.getTitle());
        }
        
        // Kullanıcı bu kitaba zaten request atmış mı kontrol et
        Optional<BorrowRequest> existingBorrowRequest = borrowRequestRepository.findByUserAndBookAndStatus(
                user, book, BorrowRequest.RequestStatus.PENDING);
        
        if (existingBorrowRequest.isPresent()) {
            throw new RuntimeException("Bu kitaba zaten ödünç alma isteği yolladınız: " + book.getTitle());
        }

        // Kullanıcı bu kitabı zaten ödünç almış mı kontrol et
        Optional<Borrow> existingBorrow = borrowRepository.findByUserAndBookAndStatus(
                user, book, Borrow.BorrowStatus.ACTIVE);
        
        if (existingBorrow.isPresent()) {
            throw new RuntimeException("Bu kitabı zaten ödünç almışsınız: " + book.getTitle());
        }
        
        // Yeni ödünç isteği oluştur
        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setUser(user);
        borrowRequest.setBook(book);
        borrowRequest.setrequestDate(LocalDate.now());
        borrowRequest.setStatus(BorrowRequest.RequestStatus.PENDING);
        
        //Rezerve içim kitabın mevcut kopya sayısını azalt
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        return borrowRequestRepository.save(borrowRequest);
    }
    
    // Borrow requesti kabul eder
    public Borrow acceptRequest(Long borrowRequestId) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(borrowRequestId)
                .orElseThrow(() -> new RuntimeException("Ödünç isteği bulunamadı: " + borrowRequestId));
        

        if (borrowRequest.getStatus() == BorrowRequest.RequestStatus.APPROVED) {
            throw new RuntimeException("Bu kitap zaten ödünç alınmış");
        }
        
        // Borrow request'i güncelle
        borrowRequest.setprocessedDate(LocalDate.now());
        borrowRequest.setStatus(BorrowRequest.RequestStatus.APPROVED);
        borrowRequestRepository.save(borrowRequest);

        // Yeni ödünç kaydı oluştur
        Borrow borrow = new Borrow();
        borrow.setUser(borrowRequest.getUser());
        borrow.setBook(borrowRequest.getBook());
        borrow.setBorrowDate(LocalDate.now());
        borrow.setStatus(Borrow.BorrowStatus.ACTIVE);
        
            return borrowRepository.save(borrow);
    }

    // Admin talebi reddeder
    public BorrowRequest rejectRequest(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Ödünç isteği bulunamadı: " + requestId));
        
        if (request.getStatus() != BorrowRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Ödünç alma isteği beklemede değil");
        }
        
        request.setStatus(BorrowRequest.RequestStatus.REJECTED);
        request.setprocessedDate(LocalDate.now());

        Book book = bookRepository.findById(request.getBook().getId())
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + request.getBook().getId()));

        //Rezerve listesinden kalktığı için kitabın mevcut kopya sayısını artır
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
        
        return borrowRequestRepository.save(request);
    }

    // Kullanıcının aktif ödünç istekleri getirir
    public List<BorrowRequest> getUserBorrowRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        

        return borrowRequestRepository.findByUser(user);
    }
    
    // Tüm ödünç isteklerini getirir
    public List<BorrowRequest> getAllBorrowRequests() {
        return borrowRequestRepository.findAll();
    }

    //    
    // Beklenen isteklerini getirir
    public List<BorrowRequest> getPendingBorrowRequests() {
        return borrowRequestRepository.findByStatus(BorrowRequest.RequestStatus.PENDING);
    }
    //

    
    // Kullanıcı adı ve kitap ID ile aktif ödünç isteğini bulur
    public BorrowRequest getActiveBorrowRequestByUsernameAndBook(String username, Long bookId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
        

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + bookId));
        
        return borrowRequestRepository.findByUserAndBookAndStatus(user, book, BorrowRequest.RequestStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("Aktif ödünç kaydı bulunamadı"));
    }
}

