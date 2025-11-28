package com.library.libman.repository;

import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

//Database işlemleri için repository classı

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    
    // Kullanıcının ödünç aldığı kitaplar
    List<Borrow> findByUser(User user);
    
    // Aktif ödünç kayıtları (henüz iade edilmemiş)
    List<Borrow> findByUserAndStatus(User user, Borrow.BorrowStatus status);
    
    // Kitaba göre ödünç kayıtları
    List<Borrow> findByBook(Book book);
    
    // Kullanıcı ve kitaba göre aktif ödünç kaydı
    Optional<Borrow> findByUserAndBookAndStatus(User user, Book book, Borrow.BorrowStatus status);
}














