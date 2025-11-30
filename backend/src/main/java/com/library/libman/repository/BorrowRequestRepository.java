package com.library.libman.repository;

import com.library.libman.entity.BorrowRequest;
import com.library.libman.entity.User;
import com.library.libman.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Database işlemleri için repository classı

@Repository
public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {
    
    // Kullanıcının ödünç istediği kitaplar
    List<BorrowRequest> findByUser(User user);
    
    // Bekleyen ödünç istekleri
    List<BorrowRequest> findByUserAndStatus(User user, BorrowRequest.RequestStatus status);

    // Bekleyen ödünç isteklerini ayırmak için
    List<BorrowRequest> findByStatus(BorrowRequest.RequestStatus status);
    
    // Kitaba göre ödünç istekleri
    List<BorrowRequest> findByBook(Book book);
    
    // Kullanıcı ve kitaba göre aktif ödünç istekleri
    Optional<BorrowRequest> findByUserAndBookAndStatus(User user, Book book, BorrowRequest.RequestStatus status);
}
