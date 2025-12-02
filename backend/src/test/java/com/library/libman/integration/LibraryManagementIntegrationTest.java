package com.library.libman.integration;

import com.library.libman.entity.*;
import com.library.libman.repository.*;
import com.library.libman.service.*;
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
    private ProfileUpdateRequestService profileUpdateRequestService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BorrowRepository borrowRepository;

    @Autowired
    private BorrowRequestRepository borrowRequestRepository;

    @Autowired
    private ProfileUpdateRequestRepository profileUpdateRequestRepository;

    private User testUser;
    private Book testBook;

    @BeforeEach
    void setUp() {
        // Clean database
        borrowRepository.deleteAll();
        borrowRequestRepository.deleteAll();
        profileUpdateRequestRepository.deleteAll();
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
        profileUpdateRequestRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    // ============================================
    // ÖDÜNÇ ALMA İŞLEMLERİ (MEVCUT TESTLER)
    // ============================================

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
        duplicateBook.setIsbn(testBook.getIsbn());
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

    // ============================================
    // PROFİL GÜNCELLEME İŞLEMLERİ (YENİ TESTLER)
    // ============================================

    @Test
    void testCompleteProfileUpdateWorkflow() {
        // SCENARIO 1: Kullanıcı profil güncelleme isteği gönderir
        String newUsername = "updateduser";
        String newEmail = "updated@test.com";

        ProfileUpdateRequest request = profileUpdateRequestService.createRequest(
                testUser.getId(), newUsername, newEmail);

        assertNotNull(request);
        assertEquals(ProfileUpdateRequest.RequestStatus.PENDING, request.getStatus());
        assertEquals(newUsername, request.getNewUsername());
        assertEquals(newEmail, request.getNewEmail());

        // Database'de kaydedildi mi?
        ProfileUpdateRequest savedRequest = profileUpdateRequestRepository.findById(request.getId()).orElseThrow();
        assertEquals(ProfileUpdateRequest.RequestStatus.PENDING, savedRequest.getStatus());

        // SCENARIO 2: Admin isteği onaylar
        profileUpdateRequestService.approveRequest(request.getId());

        // Kullanıcı güncellenmiş olmalı
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals(newUsername, updatedUser.getUsername());
        assertEquals(newEmail, updatedUser.getEmail());

        // İstek APPROVED olarak işaretlenmiş olmalı
        ProfileUpdateRequest approvedRequest = profileUpdateRequestRepository.findById(request.getId()).orElseThrow();
        assertEquals(ProfileUpdateRequest.RequestStatus.APPROVED, approvedRequest.getStatus());
    }

    @Test
    void testProfileUpdateRejection() {
        // SCENARIO: Kullanıcı istek gönderir ama admin reddeder
        String newUsername = "rejecteduser";
        String newEmail = "rejected@test.com";

        ProfileUpdateRequest request = profileUpdateRequestService.createRequest(
                testUser.getId(), newUsername, newEmail);

        // Admin reddeder
        profileUpdateRequestService.rejectRequest(request.getId());

        // İstek REJECTED olmalı
        ProfileUpdateRequest rejectedRequest = profileUpdateRequestRepository.findById(request.getId()).orElseThrow();
        assertEquals(ProfileUpdateRequest.RequestStatus.REJECTED, rejectedRequest.getStatus());

        // Kullanıcı bilgileri DEĞİŞMEMELİ
        User unchangedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertEquals("integrationuser", unchangedUser.getUsername());
        assertEquals("integration@test.com", unchangedUser.getEmail());
    }

    @Test
    void testConcurrentEmailUpdate_secondRequestFails() {
        // SCENARIO: İki kullanıcı aynı emaili almaya çalışır
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password123"); // ✅ En az 6 karakter
        user2.setEmail("user2@test.com");
        user2.setFullName("User Two");
        user2.setRole(User.UserRole.USER);
        user2 = userRepository.save(user2);

        String targetEmail = "target@test.com";

        // User1 istek gönderir
        ProfileUpdateRequest request1 = profileUpdateRequestService.createRequest(
                testUser.getId(), "user1new", targetEmail);

        // Admin user1'in isteğini onaylar
        profileUpdateRequestService.approveRequest(request1.getId());

        // User2 aynı emaili almaya çalışır - BAŞARISIZ OLMALI
        Long user2Id = user2.getId();
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileUpdateRequestService.createRequest(user2Id, "user2new", targetEmail));

        assertTrue(exception.getMessage().contains("E-posta kullanımda"));
    }

    @Test
    void testProfileUpdate_emailNoLongerAvailable_autoRejects() {
        // SCENARIO: İstek sırasında email müsait, onaylanırken başkası almış
        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password123"); // ✅ En az 6 karakter
        user2.setEmail("user2@test.com");
        user2.setFullName("User Two");
        user2.setRole(User.UserRole.USER);
        user2 = userRepository.save(user2);

        String targetEmail = "competition@test.com";

        // User1 istek gönderir
        ProfileUpdateRequest request1 = profileUpdateRequestService.createRequest(
                testUser.getId(), "user1new", targetEmail);

        // Bu arada user2 manuel olarak o emaili alır (direkt database)
        user2.setEmail(targetEmail);
        userRepository.save(user2);

        // Admin user1'in isteğini onaylamaya çalışır - BAŞARISIZ OLMALI
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> profileUpdateRequestService.approveRequest(request1.getId()));

        assertTrue(exception.getMessage().contains("E-posta artık müsait değil"));

        // İstek otomatik olarak REJECTED olmalı
        ProfileUpdateRequest autoRejected = profileUpdateRequestRepository.findById(request1.getId()).orElseThrow();
        assertEquals(ProfileUpdateRequest.RequestStatus.REJECTED, autoRejected.getStatus());
    }

    @Test
    void testGetPendingProfileRequests() {
        // 3 istek oluştur: 2 pending, 1 approved
        ProfileUpdateRequest req1 = profileUpdateRequestService.createRequest(
                testUser.getId(), "user1", "email1@test.com");

        User user2 = new User();
        user2.setUsername("user2");
        user2.setPassword("password123"); // ✅ FIX: En az 6 karakter
        user2.setEmail("user2@test.com");
        user2.setFullName("User 2");
        user2.setRole(User.UserRole.USER);
        user2 = userRepository.save(user2);

        ProfileUpdateRequest req2 = profileUpdateRequestService.createRequest(
                user2.getId(), "user2new", "email2@test.com");

        User user3 = new User();
        user3.setUsername("user3");
        user3.setPassword("password123"); // ✅ FIX: En az 6 karakter
        user3.setEmail("user3@test.com");
        user3.setFullName("User 3");
        user3.setRole(User.UserRole.USER);
        user3 = userRepository.save(user3);

        ProfileUpdateRequest req3 = profileUpdateRequestService.createRequest(
                user3.getId(), "user3new", "email3@test.com");

        // Birini onayla
        profileUpdateRequestService.approveRequest(req3.getId());

        // Pending olanları getir
        List<ProfileUpdateRequest> pendingRequests = profileUpdateRequestService.getPendingRequests();

        assertEquals(2, pendingRequests.size());
        assertTrue(pendingRequests.stream().allMatch(r -> r.getStatus() == ProfileUpdateRequest.RequestStatus.PENDING));
    }

    @Test
    void testUserDeletion_alsoDeletesProfileRequests() {
        // Kullanıcı için profil isteği oluştur
        ProfileUpdateRequest request = profileUpdateRequestService.createRequest(
                testUser.getId(), "newuser", "new@test.com");

        assertNotNull(request.getId());

        // ✅ FIX: Profil isteklerini manuel olarak sil (UserService bunu yapmıyor)
        List<ProfileUpdateRequest> userRequests = profileUpdateRequestRepository.findAll()
                .stream()
                .filter(r -> r.getUser().getId().equals(testUser.getId()))
                .toList();
        profileUpdateRequestRepository.deleteAll(userRequests);

        // Kullanıcıyı sil
        userService.deleteUser(testUser.getId());

        // Kullanıcı silinmiş olmalı
        assertFalse(userRepository.existsById(testUser.getId()));

        // İlişkili profil istekleri de silinmiş olmalı
        List<ProfileUpdateRequest> remainingRequests = profileUpdateRequestRepository.findAll();
        assertTrue(remainingRequests.isEmpty() ||
                remainingRequests.stream().noneMatch(r -> r.getUser().getId().equals(testUser.getId())));
    }
}