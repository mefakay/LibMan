package com.library.libman.service;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;

import com.library.libman.repository.BookRepository;
import com.library.libman.repository.BorrowRepository;
import com.library.libman.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class BorrowServiceTest {

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private BorrowService borrowService;

    private User user;
    private Book book;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setFullName("Test User");
        user.setEmail("test@example.com");

        book = new Book();
        book.setId(10L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890");
        book.setTotalCopies(5);
        book.setAvailableCopies(3);
        book.setPublicationYear(2024);
    }

    @Test
    void borrowBook_success_whenAvailable_andNotAlreadyBorrowed() {
        // Arrange
        Long userId = 1L;
        Long bookId = 10L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.findByUserAndBookAndStatus(
                eq(user), eq(book), eq(Borrow.BorrowStatus.ACTIVE))).thenReturn(Optional.empty());

        // borrowRepository.save() çağrısı dönecek dummy obje
        Borrow savedBorrow = new Borrow();
        savedBorrow.setId(100L);
        savedBorrow.setUser(user);
        savedBorrow.setBook(book);
        savedBorrow.setStatus(Borrow.BorrowStatus.ACTIVE);
        savedBorrow.setBorrowDate(LocalDate.now());

        when(borrowRepository.save(any(Borrow.class))).thenReturn(savedBorrow);

        // Act
        Borrow result = borrowService.borrowBook(userId, bookId);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());
        assertEquals(Borrow.BorrowStatus.ACTIVE, result.getStatus());
        assertEquals(LocalDate.now(), result.getBorrowDate());

        // availableCopies 3 -> 2 olmalı
        assertEquals(2, book.getAvailableCopies());
        verify(bookRepository).save(book);

        // Borrow kaydı gerçekten doğru parametrelerle kaydedilmiş mi, yakalayalım
        ArgumentCaptor<Borrow> borrowCaptor = ArgumentCaptor.forClass(Borrow.class);
        verify(borrowRepository).save(borrowCaptor.capture());
        Borrow captured = borrowCaptor.getValue();

        assertEquals(user, captured.getUser());
        assertEquals(book, captured.getBook());
        assertEquals(Borrow.BorrowStatus.ACTIVE, captured.getStatus());
        assertEquals(LocalDate.now(), captured.getBorrowDate());
    }

    @Test
    void borrowBook_throws_whenNoAvailableCopies() {
        // Arrange
        Long userId = 1L;
        Long bookId = 10L;

        book.setAvailableCopies(0); // hiç kopya yok

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowService.borrowBook(userId, bookId));

        assertTrue(ex.getMessage().contains("Kitap şu anda mevcut değil"));

        // availableCopies değişmemeli
        assertEquals(0, book.getAvailableCopies());

        // bookRepository.save veya borrowRepository.save çağrılmamalı
        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowRepository, never()).save(any(Borrow.class));
    }

    @Test
    void borrowBook_throws_whenUserAlreadyBorrowedSameBook() {
        // Arrange
        Long userId = 1L;
        Long bookId = 10L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        Borrow existing = new Borrow();
        existing.setId(50L);
        existing.setUser(user);
        existing.setBook(book);
        existing.setStatus(Borrow.BorrowStatus.ACTIVE);

        when(borrowRepository.findByUserAndBookAndStatus(
                eq(user), eq(book), eq(Borrow.BorrowStatus.ACTIVE))).thenReturn(Optional.of(existing));

        // Act + Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowService.borrowBook(userId, bookId));

        assertTrue(ex.getMessage().contains("Bu kitabı zaten ödünç almışsınız"));

        // Kitap kopya sayısı değişmemeli
        assertEquals(3, book.getAvailableCopies());

        // save çağrısı olmamalı
        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowRepository, never()).save(any(Borrow.class));
    }
}