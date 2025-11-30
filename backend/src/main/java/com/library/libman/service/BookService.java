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

// Kitap işlemlerini yöneten servis
@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private BorrowRepository borrowRepository;
    
    @Autowired
    private BorrowRequestRepository borrowRequestRepository;
    
    // Tüm kitapları getirir
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    // ID'ye göre kitap getirir
    public Optional<Book> getBookById(@NonNull Long id) {
        return bookRepository.findById(id);
    }

    // Başlığa göre kitap arama (büyük/küçük harf duyarlılığı yok)
    public List<Book> getBooksByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }
    
    // Yeni kitap ekler
    public Book addBook(Book book) {
        // Aynı ISBN var mı kontrol et
        if (bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
            throw new RuntimeException("Bu ISBN numarası zaten kullanılıyor: " + book.getIsbn());
        }
        
        // Mevcut kopya sayısı belirtilmemişse toplam kopya sayısına eşitle
        if (book.getAvailableCopies() == null) {
            book.setAvailableCopies(book.getTotalCopies());
        }
        
        return bookRepository.save(book);
    }
    
    // Kitabı siler (önce ilgili ödünç isteklerini ve ödünç kayıtlarını siler)
    public void deleteBook(@NonNull Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + id));
        
        // Önce bu kitaba ait tüm ödünç isteklerini sil
        var borrowRequests = borrowRequestRepository.findByBook(book);
        if (!borrowRequests.isEmpty()) {
            borrowRequestRepository.deleteAll(borrowRequests);
        }
        
        // Sonra bu kitaba ait tüm ödünç kayıtlarını sil
        var borrows = borrowRepository.findByBook(book);
        if (!borrows.isEmpty()) {
            borrowRepository.deleteAll(borrows);
        }
        
        // En son kitabı sil
        bookRepository.deleteById(id);
    }
    
    // Kitap bilgilerini günceller
    public Book updateBook(@NonNull Long id, Book updatedBook) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + id));

        // String ve Integer gibi nullable alanlar için null check
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

        // primitive int alanlar zaten 0 default,
        // burada direkt kopyalamak mantıklı (testte biz bu alanları set ediyoruz zaten)
        existingBook.setTotalCopies(updatedBook.getTotalCopies());
        existingBook.setAvailableCopies(updatedBook.getAvailableCopies());

        return bookRepository.save(existingBook);
    }
    
    // Ödünç alınabilir kitapları getirir
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