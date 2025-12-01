package com.library.libman.service;

import com.library.libman.entity.Book;
import com.library.libman.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void getAvailableBooks_shouldReturnOnlyBooksWithValidAvailableCopies() {
        // GIVEN
        Book b1 = new Book();
        b1.setId(1L);
        b1.setTitle("Book 1");
        b1.setAuthor("Author 1");
        b1.setIsbn("978-111-11-1111-1");
        b1.setPublicationYear(2024);
        b1.setTotalCopies(5);
        b1.setAvailableCopies(3); // GEÇERLİ

        Book b2 = new Book();
        b2.setId(2L);
        b2.setTitle("Book 2");
        b2.setAuthor("Author 2");
        b2.setIsbn("978-222-22-2222-2");
        b2.setPublicationYear(2024);
        b2.setTotalCopies(5);
        b2.setAvailableCopies(0); // availableCopies > 0 şartını bozuyor

        Book b3 = new Book();
        b3.setId(3L);
        b3.setTitle("Book 3");
        b3.setAuthor("Author 3");
        b3.setIsbn("978-333-33-3333-3");
        b3.setPublicationYear(2024);
        b3.setTotalCopies(5);
        b3.setAvailableCopies(6); // availableCopies <= totalCopies şartını bozuyor

        when(bookRepository.findAll()).thenReturn(List.of(b1, b2, b3));

        // WHEN
        List<Book> result = bookService.getAvailableBooks();

        // THEN
        assertEquals(1, result.size(), "Sadece 1 kitap döndürülmeli");
        assertEquals("Book 1", result.get(0).getTitle());
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void updateBook_shouldMergeFieldsAndSave() {
        // GIVEN
        Long id = 1L;

        Book existing = new Book();
        existing.setId(id);
        existing.setTitle("Old Title");
        existing.setAuthor("Old Author");
        existing.setIsbn("978-123-45-6789-0");
        existing.setPublicationYear(2000);
        existing.setTotalCopies(10);
        existing.setAvailableCopies(5);

        when(bookRepository.findById(id)).thenReturn(Optional.of(existing));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        // Sadece bazı alanları güncelleyen "updatedBook"
        Book updated = new Book();
        updated.setTitle("New Title");
        updated.setAuthor("New Author");
        // isbn null -> değişmeyecek
        updated.setPublicationYear(2020);
        updated.setTotalCopies(15);
        updated.setAvailableCopies(7);

        // WHEN
        Book result = bookService.updateBook(id, updated);

        // THEN
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository, times(1)).save(captor.capture());
        Book saved = captor.getValue();

        // Güncellenmiş alanlar
        assertEquals("New Title", saved.getTitle());
        assertEquals("New Author", saved.getAuthor());
        assertEquals(2020, saved.getPublicationYear());
        assertEquals(15, saved.getTotalCopies());
        assertEquals(7, saved.getAvailableCopies());

        // Dokunulmayan alan (isbn)
        assertEquals("978-123-45-6789-0", saved.getIsbn());

        // Dönen obje de aynı olmalı
        assertSame(saved, result);
    }

    @Test
    void updateBook_shouldThrowWhenBookNotFound() {
        // GIVEN
        Long id = 99L;
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        Book updated = new Book();
        updated.setTitle("Doesn't matter");

        // WHEN & THEN
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> bookService.updateBook(id, updated));

        assertTrue(ex.getMessage().contains("Kitap bulunamadı"));
    }

    @Test
    void addBook_shouldSetAvailableCopiesWhenNull() {
        // GIVEN
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setIsbn("978-999-99-9999-9");
        newBook.setPublicationYear(2024);
        newBook.setTotalCopies(5);
        // availableCopies null bırakıldı

        when(bookRepository.findByIsbn(newBook.getIsbn())).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> {
            Book saved = inv.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        // WHEN
        Book result = bookService.addBook(newBook);

        // THEN
        assertNotNull(result.getId());
        assertEquals(5, result.getAvailableCopies(), "availableCopies totalCopies'a eşitlenmeli");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_shouldThrowWhenISBNExists() {
        // GIVEN
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setIsbn("978-111-11-1111-1");

        Book newBook = new Book();
        newBook.setIsbn("978-111-11-1111-1");

        when(bookRepository.findByIsbn(newBook.getIsbn())).thenReturn(Optional.of(existingBook));

        // WHEN & THEN
        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> bookService.addBook(newBook));

        assertTrue(ex.getMessage().contains("ISBN numarası zaten kullanılıyor"));
        verify(bookRepository, never()).save(any(Book.class));
    }
}