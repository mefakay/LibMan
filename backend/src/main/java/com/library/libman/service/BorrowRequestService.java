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

    // ödünç isteği
    public BorrowRequest borrowRequestBook(Long userId, Long bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        

         Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + bookId));
        
        // mevcut mu
        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("Kitap şu anda mevcut değil: " + book.getTitle());
        }
        
        // request  kontrol et
        Optional<BorrowRequest> existingBorrowRequest = borrowRequestRepository.findByUserAndBookAndStatus(
                user, book, BorrowRequest.RequestStatus.PENDING);
        
        if (existingBorrowRequest.isPresent()) {
            throw new RuntimeException("Bu kitaba zaten ödünç alma isteği yolladınız: " + book.getTitle());
        }

        // almış mı
        Optional<Borrow> existingBorrow = borrowRepository.findByUserAndBookAndStatus(
                user, book, Borrow.BorrowStatus.ACTIVE);
        
        if (existingBorrow.isPresent()) {
            throw new RuntimeException("Bu kitabı zaten ödünç almışsınız: " + book.getTitle());
        }
        
        //ödünç isteği
        BorrowRequest borrowRequest = new BorrowRequest();
        borrowRequest.setUser(user);
        borrowRequest.setBook(book);
        borrowRequest.setRequestDate(LocalDate.now());
        borrowRequest.setStatus(BorrowRequest.RequestStatus.PENDING);
        
        //mevcut azalt
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        return borrowRequestRepository.save(borrowRequest);
    }
    
    // equest kabul
    public Borrow acceptRequest(Long borrowRequestId) {
        BorrowRequest borrowRequest = borrowRequestRepository.findById(borrowRequestId)
                .orElseThrow(() -> new RuntimeException("Ödünç isteği bulunamadı: " + borrowRequestId));
        

        if (borrowRequest.getStatus() == BorrowRequest.RequestStatus.APPROVED) {
            throw new RuntimeException("Bu kitap zaten ödünç alınmış");
        }
        
        //requestgüncelle
        borrowRequest.setProcessedDate(LocalDate.now());
        borrowRequest.setStatus(BorrowRequest.RequestStatus.APPROVED);
        borrowRequestRepository.save(borrowRequest);

        // ödünç kaydı
        Borrow borrow = new Borrow();
        borrow.setUser(borrowRequest.getUser());
        borrow.setBook(borrowRequest.getBook());
        borrow.setBorrowDate(LocalDate.now());
        borrow.setStatus(Borrow.BorrowStatus.ACTIVE);
        
            return borrowRepository.save(borrow);
    }

    // red
    public BorrowRequest rejectRequest(Long requestId) {
        BorrowRequest request = borrowRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Ödünç isteği bulunamadı: " + requestId));
        
        if (request.getStatus() != BorrowRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Ödünç alma isteği beklemede değil");
        }
        
        request.setStatus(BorrowRequest.RequestStatus.REJECTED);
        request.setProcessedDate(LocalDate.now());

        Book book = bookRepository.findById(request.getBook().getId())
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + request.getBook().getId()));

        //kopya artır
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);
        
        return borrowRequestRepository.save(request);
    }

    public List<BorrowRequest> getUserBorrowRequests(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + userId));
        

        return borrowRequestRepository.findByUser(user);
    }
    
    // ödünç istekleri
    public List<BorrowRequest> getAllBorrowRequests() {
        return borrowRequestRepository.findAll();
    }


    // Beklenen istekler
    public List<BorrowRequest> getPendingBorrowRequests() {
        return borrowRequestRepository.findByStatus(BorrowRequest.RequestStatus.PENDING);
    }

    
    // ödünç isteğini bulur
    public BorrowRequest getActiveBorrowRequestByUsernameAndBook(String username, Long bookId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı: " + username));
        

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + bookId));
        
        return borrowRequestRepository.findByUserAndBookAndStatus(user, book, BorrowRequest.RequestStatus.PENDING)
                .orElseThrow(() -> new RuntimeException("Aktif ödünç kaydı bulunamadı"));
    }
}

