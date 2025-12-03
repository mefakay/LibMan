package com.library.libman.service;

import com.library.libman.entity.Book;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.BorrowRepository;
import com.library.libman.repository.BorrowRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private BorrowRequestRepository borrowRequestRepository;
    
    // kitapları getirir
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    // ID kitap getirir
    public Optional<Book> getBookById(@NonNull Long id) {
        return bookRepository.findById(id);
    }

    //arama
    public List<Book> getBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    // Yeni kitap
    public Book addBook(Book book) {
        if (bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
            throw new RuntimeException("Bu ISBN numarası zaten kullanılıyor: " + book.getIsbn());
        }

        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        
        return bookRepository.save(book);
    }
    
    // siler
    public void deleteBook(@NonNull Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + id));
        
        // ödünç isteklerini sil
        var borrowRequests = borrowRequestRepository.findByBook(book);
        if (!borrowRequests.isEmpty()) {
            borrowRequestRepository.deleteAll(borrowRequests);
        }
        
        // ödünç kayıtlarını sil
        var borrows = borrowRepository.findByBook(book);
        if (!borrows.isEmpty()) {
            borrowRepository.deleteAll(borrows);
        }
        
        // sil
        bookRepository.deleteById(id);
    }
    
    // Kitap bilgilerini deüitir
    public Book updateBook(@NonNull Long id, Book updatedBook) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + id));

        if (updatedBook.getTitle() != null && !updatedBook.getTitle().isBlank()) {
            existingBook.setTitle(updatedBook.getTitle());
        }

        if (updatedBook.getAuthor() != null && !updatedBook.getAuthor().isBlank()) {
            existingBook.setAuthor(updatedBook.getAuthor());
        }

        if (updatedBook.getIsbn() != null && !updatedBook.getIsbn().isBlank()) {
            existingBook.setIsbn(updatedBook.getIsbn());
        }

        if (updatedBook.getPublicationYear() != null) {
            existingBook.setPublicationYear(updatedBook.getPublicationYear());
        }

        existingBook.setTotalCopies(updatedBook.getTotalCopies());
        existingBook.setAvailableCopies(updatedBook.getAvailableCopies());

        return bookRepository.save(existingBook);
    }

    public List<Book> getAvailableBooks() {
    return bookRepository.findAll()
            .stream()
            .filter(book ->
                    book.getAvailableCopies() > 0 &&
                    book.getAvailableCopies() <= book.getTotalCopies()
            )
            .collect(Collectors.toList());
    }

}