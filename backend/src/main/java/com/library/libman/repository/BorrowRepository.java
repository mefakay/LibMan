package com.library.libman.repository;

import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    //ödünç aldığı kitaplar
    List<Borrow> findByUser(User user);
    
    // ödünç kayıtları
    List<Borrow> findByUserAndStatus(User user, Borrow.BorrowStatus status);

    // ödünç kayıtları
    List<Borrow> findByBook(Book book);
    
    //ödünç kaydı
    Optional<Borrow> findByUserAndBookAndStatus(User user, Book book, Borrow.BorrowStatus status);
}














