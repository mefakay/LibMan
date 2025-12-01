package com.library.libman.service;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.BorrowRequest;
import com.library.libman.entity.User;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.BorrowRepository;
import com.library.libman.repository.BorrowRequestRepository;
import com.library.libman.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowRequestServiceTest {

    @Mock
    private BorrowRequestRepository borrowRequestRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BorrowRepository borrowRepository;

    @InjectMocks
    private BorrowRequestService borrowRequestService;

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
        book.setIsbn("978-123-45-6789-0");
        book.setTotalCopies(5);
        book.setAvailableCopies(3);
        book.setPublicationYear(2024);
    }

    @Test
    void borrowRequestBook_success_whenBookAvailable() {
        // GIVEN
        Long userId = 1L;
        Long bookId = 10L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRequestRepository.findByUserAndBookAndStatus(
                eq(user), eq(book), eq(BorrowRequest.RequestStatus.PENDING)))
                .thenReturn(Optional.empty());
        when(borrowRepository.findByUserAndBookAndStatus(
                eq(user), eq(book), eq(Borrow.BorrowStatus.ACTIVE)))
                .thenReturn(Optional.empty());

        BorrowRequest savedRequest = new BorrowRequest();
        savedRequest.setId(100L);
        savedRequest.setUser(user);
        savedRequest.setBook(book);
        savedRequest.setStatus(BorrowRequest.RequestStatus.PENDING);
        savedRequest.setRequestDate(LocalDate.now());

        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenReturn(savedRequest);

        // WHEN
        BorrowRequest result = borrowRequestService.borrowRequestBook(userId, bookId);

        // THEN
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());
        assertEquals(BorrowRequest.RequestStatus.PENDING, result.getStatus());
        assertEquals(LocalDate.now(), result.getRequestDate());

        // Available copies should decrease by 1 (reservation)
        assertEquals(2, book.getAvailableCopies());
        verify(bookRepository).save(book);
        verify(borrowRequestRepository).save(any(BorrowRequest.class));
    }

    @Test
    void borrowRequestBook_throws_whenBookNotAvailable() {
        // GIVEN
        Long userId = 1L;
        Long bookId = 10L;
        book.setAvailableCopies(0);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowRequestService.borrowRequestBook(userId, bookId));

        assertTrue(ex.getMessage().contains("Kitap şu anda mevcut değil"));
        verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void borrowRequestBook_throws_whenPendingRequestExists() {
        // GIVEN
        Long userId = 1L;
        Long bookId = 10L;

        BorrowRequest existingRequest = new BorrowRequest();
        existingRequest.setId(50L);
        existingRequest.setUser(user);
        existingRequest.setBook(book);
        existingRequest.setStatus(BorrowRequest.RequestStatus.PENDING);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRequestRepository.findByUserAndBookAndStatus(
                eq(user), eq(book), eq(BorrowRequest.RequestStatus.PENDING)))
                .thenReturn(Optional.of(existingRequest));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowRequestService.borrowRequestBook(userId, bookId));

        assertTrue(ex.getMessage().contains("Bu kitaba zaten ödünç alma isteği yolladınız"));
        verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
    }

    @Test
    void borrowRequestBook_throws_whenUserAlreadyBorrowedBook() {
        // GIVEN
        Long userId = 1L;
        Long bookId = 10L;

        Borrow activeBorrow = new Borrow();
        activeBorrow.setId(30L);
        activeBorrow.setUser(user);
        activeBorrow.setBook(book);
        activeBorrow.setStatus(Borrow.BorrowStatus.ACTIVE);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRequestRepository.findByUserAndBookAndStatus(
                eq(user), eq(book), eq(BorrowRequest.RequestStatus.PENDING)))
                .thenReturn(Optional.empty());
        when(borrowRepository.findByUserAndBookAndStatus(
                eq(user), eq(book), eq(Borrow.BorrowStatus.ACTIVE)))
                .thenReturn(Optional.of(activeBorrow));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowRequestService.borrowRequestBook(userId, bookId));

        assertTrue(ex.getMessage().contains("Bu kitabı zaten ödünç almışsınız"));
        verify(borrowRequestRepository, never()).save(any(BorrowRequest.class));
    }

    @Test
    void acceptRequest_success_convertsRequestToBorrow() {
        // GIVEN
        Long requestId = 100L;

        BorrowRequest request = new BorrowRequest();
        request.setId(requestId);
        request.setUser(user);
        request.setBook(book);
        request.setStatus(BorrowRequest.RequestStatus.PENDING);
        request.setRequestDate(LocalDate.now().minusDays(1));

        when(borrowRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        Borrow savedBorrow = new Borrow();
        savedBorrow.setId(200L);
        savedBorrow.setUser(user);
        savedBorrow.setBook(book);
        savedBorrow.setStatus(Borrow.BorrowStatus.ACTIVE);
        savedBorrow.setBorrowDate(LocalDate.now());

        when(borrowRepository.save(any(Borrow.class))).thenReturn(savedBorrow);

        // WHEN
        Borrow result = borrowRequestService.acceptRequest(requestId);

        // THEN
        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertEquals(user, result.getUser());
        assertEquals(book, result.getBook());
        assertEquals(Borrow.BorrowStatus.ACTIVE, result.getStatus());

        // Verify request was updated
        ArgumentCaptor<BorrowRequest> requestCaptor = ArgumentCaptor.forClass(BorrowRequest.class);
        verify(borrowRequestRepository).save(requestCaptor.capture());
        BorrowRequest updatedRequest = requestCaptor.getValue();
        assertEquals(BorrowRequest.RequestStatus.APPROVED, updatedRequest.getStatus());
        assertEquals(LocalDate.now(), updatedRequest.getProcessedDate());

        // Verify borrow was created
        verify(borrowRepository).save(any(Borrow.class));
    }

    @Test
    void acceptRequest_throws_whenAlreadyApproved() {
        // GIVEN
        Long requestId = 100L;

        BorrowRequest request = new BorrowRequest();
        request.setId(requestId);
        request.setUser(user);
        request.setBook(book);
        request.setStatus(BorrowRequest.RequestStatus.APPROVED);

        when(borrowRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowRequestService.acceptRequest(requestId));

        assertTrue(ex.getMessage().contains("Bu kitap zaten ödünç alınmış"));
        verify(borrowRepository, never()).save(any(Borrow.class));
    }

    @Test
    void rejectRequest_success_updatesStatusAndRestoresAvailability() {
        // GIVEN
        Long requestId = 100L;

        BorrowRequest request = new BorrowRequest();
        request.setId(requestId);
        request.setUser(user);
        request.setBook(book);
        request.setStatus(BorrowRequest.RequestStatus.PENDING);

        when(borrowRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(borrowRequestRepository.save(any(BorrowRequest.class))).thenAnswer(inv -> inv.getArgument(0));

        int initialAvailable = book.getAvailableCopies();

        // WHEN
        BorrowRequest result = borrowRequestService.rejectRequest(requestId);

        // THEN
        assertNotNull(result);
        assertEquals(BorrowRequest.RequestStatus.REJECTED, result.getStatus());
        assertEquals(LocalDate.now(), result.getProcessedDate());

        // Available copies should increase by 1 (released from reservation)
        assertEquals(initialAvailable + 1, book.getAvailableCopies());
        verify(bookRepository).save(book);
        verify(borrowRequestRepository).save(request);
    }

    @Test
    void rejectRequest_throws_whenNotPending() {
        // GIVEN
        Long requestId = 100L;

        BorrowRequest request = new BorrowRequest();
        request.setId(requestId);
        request.setStatus(BorrowRequest.RequestStatus.APPROVED);

        when(borrowRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

        // WHEN & THEN
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> borrowRequestService.rejectRequest(requestId));

        assertTrue(ex.getMessage().contains("Ödünç alma isteği beklemede değil"));
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void getUserBorrowRequests_returnsUserRequests() {
        // GIVEN
        Long userId = 1L;

        BorrowRequest req1 = new BorrowRequest();
        req1.setId(1L);
        req1.setUser(user);
        req1.setBook(book);
        req1.setStatus(BorrowRequest.RequestStatus.PENDING);

        BorrowRequest req2 = new BorrowRequest();
        req2.setId(2L);
        req2.setUser(user);
        req2.setBook(book);
        req2.setStatus(BorrowRequest.RequestStatus.APPROVED);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(borrowRequestRepository.findByUser(user)).thenReturn(Arrays.asList(req1, req2));

        // WHEN
        List<BorrowRequest> result = borrowRequestService.getUserBorrowRequests(userId);

        // THEN
        assertEquals(2, result.size());
        verify(borrowRequestRepository).findByUser(user);
    }

    @Test
    void getPendingBorrowRequests_returnsOnlyPending() {
        // GIVEN
        BorrowRequest pending1 = new BorrowRequest();
        pending1.setId(1L);
        pending1.setStatus(BorrowRequest.RequestStatus.PENDING);

        BorrowRequest pending2 = new BorrowRequest();
        pending2.setId(2L);
        pending2.setStatus(BorrowRequest.RequestStatus.PENDING);

        when(borrowRequestRepository.findByStatus(BorrowRequest.RequestStatus.PENDING))
                .thenReturn(Arrays.asList(pending1, pending2));

        // WHEN
        List<BorrowRequest> result = borrowRequestService.getPendingBorrowRequests();

        // THEN
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getStatus() == BorrowRequest.RequestStatus.PENDING));
        verify(borrowRequestRepository).findByStatus(BorrowRequest.RequestStatus.PENDING);
    }

    @Test
    void getAllBorrowRequests_returnsAll() {
        // GIVEN
        BorrowRequest req1 = new BorrowRequest();
        req1.setId(1L);

        BorrowRequest req2 = new BorrowRequest();
        req2.setId(2L);

        when(borrowRequestRepository.findAll()).thenReturn(Arrays.asList(req1, req2));

        // WHEN
        List<BorrowRequest> result = borrowRequestService.getAllBorrowRequests();

        // THEN
        assertEquals(2, result.size());
        verify(borrowRequestRepository).findAll();
    }
}