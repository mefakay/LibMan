package com.library.libman.repository;

import com.library.libman.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;


@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    //kitap arama
    Optional<Book> findByTitle(String title);

    //büyükküçük harf duyarlılığı yok
    List<Book> findByTitleContainingIgnoreCase(String title);
}



