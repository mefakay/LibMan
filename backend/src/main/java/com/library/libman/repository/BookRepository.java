package com.library.libman.repository;

import com.library.libman.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

//Database işlemleri için repository classı

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    // ISBN'e göre kitap bulma
    Optional<Book> findByIsbn(String isbn);
    
    // Başlığa göre kitap arama
    Optional<Book> findByTitle(String title);
}



