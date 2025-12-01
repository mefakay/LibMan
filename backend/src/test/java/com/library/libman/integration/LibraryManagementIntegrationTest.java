package com.library.libman.integration;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.BorrowRequest;
import com.library.libman.entity.User;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.BorrowRepository;
import com.library.libman.repository.BorrowRequestRepository;
import com.library.libman.repository.UserRepository;
import com.library.libman.service.BookService;
import com.library.libman.service.BorrowRequestService;
import com.library.libman.service.BorrowService;
import com.library.libman.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Test - Gerçek veritabanı ile test eder
 * H2 in-memory database kullanır
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
class LibraryManagementIntegrationTest {

    @Autowired
    private BookService bookService;

    @Autowired
    private UserService userService;

    @Autowired
    private BorrowService borrowService;

    @Autowired
    private BorrowRequestService borrowRequestService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Clean database
        borrowRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("integrationuser");
        testUser.setPassword("password123");
        testUser.setEmail("integration@test.com");
        testUser.setFullName("Integration Test User");
        testUser.setRole(User.UserRole.USER);
        testUser = userRepository.save(testUser);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Integration Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("978-999-99-9999-9");
        testBook.setPublicationYear(2024);
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(5);
        testBook = bookRepository.save(testBook);
    }

    @AfterEach
    void tearDown() {
        borrowRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testCompleteBookBorrowingWorkflow() {
        // SCENARIO 1: Kullanıcı kitap ödünç alma isteği gönderir
        BorrowRequest request = borrowRequestService.borrowRequestBook(
                testUser.getId(), testBook.getId());

        assertNotNull(request);
        assertEquals(BorrowRequest.RequestStatus.PENDING, request.getStatus());

        // Kitap rezerve edildi, available copies azalmalı
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(4, updatedBook.getAvailableCopies());

        // SCENARIO 2: Admin isteği onaylar
        Borrow borrow = borrowRequestService.acceptRequest(request.getId());

        assertNotNull(borrow);
        assertEquals(Borrow.BorrowStatus.ACTIVE, borrow.getStatus());
        assertEquals(testUser.getId(), borrow.getUser().getId());
        assertEquals(testBook.getId(), borrow.getBook().getId());

        // İstek onaylandı mı kontrol et
        BorrowRequest approvedRequest = borrowRequestRepository.findById(request.getId()).orElseThrow();
        assertEquals(BorrowRequest.RequestStatus.APPROVED, approvedRequest.getStatus());

        // SCENARIO 3: Kullanıcı kitabı iade eder
        Borrow returnedBorrow = borrowService.returnBook(borrow.getId());

        assertEquals(Borrow.BorrowStatus.RETURNED, returnedBorrow.getStatus());
        assertNotNull(returnedBorrow.getReturnDate());

        // İade sonrası available copies artmalı
        Book finalBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(5, finalBook.getAvailableCopies());
    }

    @Test
    void testMultipleUsersBorrowingSameBook() {
        // İkinci kullanıcı oluştur
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password123");
        user2.setEmail("user2@test.com");
        user2.setFullName("User Two");
        user2.setRole(User.UserRole.USER);
        user2 = userRepository.save(user2);

        // Her iki kullanıcı da aynı kitabı ödünç alır
        Borrow borrow1 = borrowService.borrowBook(testUser.getId(), testBook.getId());
        Borrow borrow2 = borrowService.borrowBook(user2.getId(), testBook.getId());

        assertNotNull(borrow1);
        assertNotNull(borrow2);

        // Available copies 5'ten 3'e düşmeli
        Book updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(3, updatedBook.getAvailableCopies());

        // İlk kullanıcı iade eder
        borrowService.returnBook(borrow1.getId());

        // Available copies 4'e çıkmalı
        updatedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(4, updatedBook.getAvailableCopies());
    }

    @Test
    void testBorrowRequestRejection() {
        // İstek gönder
        BorrowRequest request = borrowRequestService.borrowRequestBook(
                testUser.getId(), testBook.getId());

        // Rezerve edildi
        Book reservedBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(4, reservedBook.getAvailableCopies());

        // İsteği reddet
        BorrowRequest rejectedRequest = borrowRequestService.rejectRequest(request.getId());

        assertEquals(BorrowRequest.RequestStatus.REJECTED, rejectedRequest.getStatus());

        // Rezervasyon kaldırıldı, available copies eski haline dönmeli
        Book restoredBook = bookRepository.findById(testBook.getId()).orElseThrow();
        assertEquals(5, restoredBook.getAvailableCopies());
    }

    @Test
    void testUserCannotBorrowSameBookTwice() {
        // İlk ödünç alma
        Borrow borrow1 = borrowService.borrowBook(testUser.getId(), testBook.getId());
        assertNotNull(borrow1);

        // Aynı kitabı tekrar ödünç almaya çalışır
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> borrowService.borrowBook(testUser.getId(), testBook.getId()));

        assertTrue(exception.getMessage().contains("Bu kitabı zaten ödünç almışsınız"));
    }

    @Test
    void testBookDeletion_alsoDeletesRelatedRecords() {
        // Önce ödünç kaydı oluştur
        Borrow borrow = borrowService.borrowBook(testUser.getId(), testBook.getId());
        assertNotNull(borrow);

        // Kitabı sil (ilişkili kayıtlar da silinmeli)
        bookService.deleteBook(testBook.getId());

        // Kitap silinmiş olmalı
        assertFalse(bookRepository.existsById(testBook.getId()));

        // İlişkili ödünç kayıtları da silinmiş olmalı
        List<Borrow> borrows = borrowRepository.findAll();
        assertTrue(borrows.isEmpty() || borrows.stream().noneMatch(b -> b.getBook().getId().equals(testBook.getId())));
    }

    @Test
    void testAddBookWithValidation() {
        Book validBook = new Book();
        validBook.setTitle("Valid Book");
        validBook.setAuthor("Valid Author");
        validBook.setIsbn("978-111-11-1111-1");
        validBook.setPublicationYear(2024);
        validBook.setTotalCopies(3);
        validBook.setAvailableCopies(3);

        Book savedBook = bookService.addBook(validBook);

        assertNotNull(savedBook.getId());
        assertEquals("Valid Book", savedBook.getTitle());
        assertEquals(3, savedBook.getAvailableCopies());
    }

    @Test
    void testAddBookWithDuplicateISBN_shouldFail() {
        Book duplicateBook = new Book();
        duplicateBook.setTitle("Duplicate ISBN Book");
        duplicateBook.setAuthor("Author");
        duplicateBook.setIsbn(testBook.getIsbn()); // Same ISBN as testBook
        duplicateBook.setTotalCopies(1);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> bookService.addBook(duplicateBook));

        assertTrue(exception.getMessage().contains("ISBN numarası zaten kullanılıyor"));
    }

    @Test
    void testSearchBooksByTitle() {
        // Birkaç kitap ekle
        Book book1 = new Book();
        book1.setTitle("Java Programming");
        book1.setAuthor("Author 1");
        book1.setIsbn("978-111-11-1111-1");
        book1.setPublicationYear(2024);
        book1.setTotalCopies(5);
        book1.setAvailableCopies(5);
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setTitle("Advanced Java");
        book2.setAuthor("Author 2");
        book2.setIsbn("978-222-22-2222-2");
        book2.setPublicationYear(2024);
        book2.setTotalCopies(3);
        book2.setAvailableCopies(3);
        bookRepository.save(book2);

        // "Java" kelimesini ara
        List<Book> results = bookService.getBooksByTitle("Java");

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(b -> b.getTitle().contains("Java")));
    }

    @Test
    void testGetUserBorrowHistory() {
        // Kullanıcı birkaç kitap ödünç alsın
        Borrow borrow1 = borrowService.borrowBook(testUser.getId(), testBook.getId());

        Book book2 = new Book();
        book2.setTitle("Second Book");
        book2.setAuthor("Author 2");
        book2.setIsbn("978-888-88-8888-8");
        book2.setTotalCopies(2);
        book2.setAvailableCopies(2);
        book2 = bookRepository.save(book2);

        Borrow borrow2 = borrowService.borrowBook(testUser.getId(), book2.getId());

        // İlk kitabı iade et
        borrowService.returnBook(borrow1.getId());

        // Kullanıcının tüm ödünç geçmişini getir
        List<Borrow> history = borrowService.getUserBorrows(testUser.getId());

        assertEquals(2, history.size());

        // Aktif ödünç kayıtlarını getir
        List<Borrow> active = borrowService.getUserActiveBorrows(testUser.getId());

        assertEquals(1, active.size());
        assertEquals(book2.getId(), active.get(0).getBook().getId());
    }
}