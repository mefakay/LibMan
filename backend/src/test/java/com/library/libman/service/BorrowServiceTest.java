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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
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
        user.setUsername("ahmet");

        book = new Book();
        book.setId(10L);
        book.setTitle("Suç ve Ceza");
        book.setTotalCopies(5);
        book.setAvailableCopies(3);
    }

    @Test
    void borrowBook_success_decreasesAvailableCopies_andCreatesActiveBorrow() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(borrowRepository.findByUserAndBookAndStatus(user, book, Borrow.BorrowStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(borrowRepository.save(any(Borrow.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Borrow borrow = borrowService.borrowBook(1L, 10L);

        assertEquals(user, borrow.getUser());
        assertEquals(book, borrow.getBook());
        assertEquals(Borrow.BorrowStatus.ACTIVE, borrow.getStatus());
        assertNotNull(borrow.getBorrowDate());
        assertEquals(2, book.getAvailableCopies()); // 3 -> 2
        verify(bookRepository).save(book);
    }

    @Test
    void borrowBook_throws_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowService.borrowBook(1L, 10L));

        assertTrue(ex.getMessage().contains("Kullanıcı bulunamadı"));
    }

    @Test
    void borrowBook_throws_whenBookNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowService.borrowBook(1L, 10L));

        assertTrue(ex.getMessage().contains("Kitap bulunamadı"));
    }

    @Test
    void borrowBook_throws_whenNoAvailableCopies() {
        book.setAvailableCopies(0);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowService.borrowBook(1L, 10L));

        assertTrue(ex.getMessage().contains("Kitap şu anda mevcut değil"));
    }

    @Test
    void borrowBook_throws_whenAlreadyActiveBorrowExists() {
        Borrow existing = new Borrow();
        existing.setUser(user);
        existing.setBook(book);
        existing.setStatus(Borrow.BorrowStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookRepository.findById(10L)).thenReturn(Optional.of(book));
        when(borrowRepository.findByUserAndBookAndStatus(user, book, Borrow.BorrowStatus.ACTIVE))
                .thenReturn(Optional.of(existing));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowService.borrowBook(1L, 10L));

        assertTrue(ex.getMessage().contains("zaten ödünç almışsınız"));
    }

    @Test
    void returnBook_success_setsReturnedAndIncrementsAvailableCopies() {
        Borrow borrow = new Borrow();
        borrow.setId(5L);
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setStatus(Borrow.BorrowStatus.ACTIVE);
        borrow.setBorrowDate(LocalDate.now().minusDays(1));

        when(borrowRepository.findById(5L)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(inv -> inv.getArgument(0));

        Borrow result = borrowService.returnBook(5L);

        assertEquals(Borrow.BorrowStatus.RETURNED, result.getStatus());
        assertNotNull(result.getReturnDate());
        assertEquals(4, book.getAvailableCopies()); // 3 -> 4
        verify(bookRepository).save(book);
    }

    @Test
    void returnBook_throws_whenAlreadyReturned() {
        Borrow borrow = new Borrow();
        borrow.setId(5L);
        borrow.setStatus(Borrow.BorrowStatus.RETURNED);

        when(borrowRepository.findById(5L)).thenReturn(Optional.of(borrow));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowService.returnBook(5L));

        assertTrue(ex.getMessage().contains("zaten iade edilmiş"));
    }

    @Test
    void getUserActiveBorrows_returnsOnlyActive() {
        Borrow active = new Borrow();
        active.setStatus(Borrow.BorrowStatus.ACTIVE);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(borrowRepository.findByUserAndStatus(user, Borrow.BorrowStatus.ACTIVE))
                .thenReturn(List.of(active));

        var list = borrowService.getUserActiveBorrows(1L);

        assertEquals(1, list.size());
        assertSame(active, list.get(0));
    }
}