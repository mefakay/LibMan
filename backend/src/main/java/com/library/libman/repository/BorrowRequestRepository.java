package com.library.libman.repository;

import com.library.libman.entity.BorrowRequest;
import com.library.libman.entity.User;
import com.library.libman.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    
    //  kitapları ödünç iste
    List<BorrowRequest> findByUser(User user);

    // ödünç isteklerini bekle
    List<BorrowRequest> findByUserAndStatus(User user, BorrowRequest.RequestStatus status);

    // ayırma
    List<BorrowRequest> findByStatus(BorrowRequest.RequestStatus status);
    
    // ödünç istekleri
    List<BorrowRequest> findByBook(Book book);
    
    // ödünç istekleri
    Optional<BorrowRequest> findByUserAndBookAndStatus(User user, Book book, BorrowRequest.RequestStatus status);
}
