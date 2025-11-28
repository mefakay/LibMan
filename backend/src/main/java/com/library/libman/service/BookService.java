package com.library.libman.service;

import com.library.libman.entity.Book;
import com.library.libman.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// Kitap işlemlerini yöneten servis
@Service
public class BookService {
    
    @Autowired
    private BookRepository bookRepository;
    
    // Tüm kitapları getirir
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }
    
    // ID'ye göre kitap getirir
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
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
    
    // Kitabı siler
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new RuntimeException("Kitap bulunamadı: " + id);
        }
        bookRepository.deleteById(id);
    }
    
    // Kitap bilgilerini günceller
    public Book updateBook(Long id, Book updatedBook) {
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Kitap bulunamadı: " + id));
        
        existingBook.setTitle(updatedBook.getTitle());
        existingBook.setAuthor(updatedBook.getAuthor());
        existingBook.setIsbn(updatedBook.getIsbn());
        existingBook.setPublicationYear(updatedBook.getPublicationYear());
        existingBook.setTotalCopies(updatedBook.getTotalCopies());
        
        // Mevcut kopya sayısı da güncellenmişse onu da güncelle
        if (updatedBook.getAvailableCopies() != null) {
            existingBook.setAvailableCopies(updatedBook.getAvailableCopies());
        }
        
        return bookRepository.save(existingBook);
    }
    
    // Ödünç alınabilir kitapları getirir
    public List<Book> getAvailableBooks() {
        return bookRepository.findAll().stream()
                .filter(book -> book.getAvailableCopies() > 0)
                .toList();
    }
}

