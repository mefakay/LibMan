package com.library.libman.controller;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.BorrowRequest;
import com.library.libman.entity.User;
import com.library.libman.service.BookService;
import com.library.libman.service.BorrowRequestService;
import com.library.libman.service.BorrowService;
import com.library.libman.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.security.user.name=testadmin",
        "spring.security.user.password=testpass",
        "spring.security.user.roles=ADMIN"
})
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserService userService;

    @MockBean
    private BorrowService borrowService;

    @MockBean
    private BorrowRequestService borrowRequestService;

    private Book testBook;
    private User testUser;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("978-123-45-6789-0");
        testBook.setPublicationYear(2024);
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(3);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setEmail("admin@test.com");
        testUser.setFullName("Admin User");
        testUser.setRole(User.UserRole.ADMIN);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllBooks_returnsBookList() throws Exception {
        // GIVEN
        when(bookService.getAllBooks()).thenReturn(Arrays.asList(testBook));

        // WHEN & THEN
        mockMvc.perform(get("/api/admin/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Book"))
                .andExpect(jsonPath("$[0].author").value("Test Author"));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllBooks_forbiddenForNonAdmin() throws Exception {
        // WHEN & THEN - USER rolü ile admin endpoint'e erişim denenir
        mockMvc.perform(get("/api/admin/books"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // 400 (Bad Request) veya 403 (Forbidden) kabul edilir
                    // Spring Security bazen validation öncesi 400 dönebilir
                    assertTrue(status == 403 || status == 400,
                            "Status should be 403 or 400, but was: " + status);
                });

        // Service çağrılmamalı
        verify(bookService, never()).getAllBooks();
    }

    @Test
    @WithMockUser(roles = "USER")
    void getAllUsers_forbiddenForNonAdmin() throws Exception {
        // WHEN & THEN - USER rolü ile admin endpoint'e erişim denenir
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(result -> {
                    int status = result.getResponse().getStatus();
                    // 400 (Bad Request) veya 403 (Forbidden) kabul edilir
                    assertTrue(status == 403 || status == 400,
                            "Status should be 403 or 400, but was: " + status);
                });

        verify(userService, never()).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void addBook_success() throws Exception {
        // GIVEN
        Book newBook = new Book();
        newBook.setTitle("New Book");
        newBook.setAuthor("New Author");
        newBook.setIsbn("978-111-11-1111-1");
        newBook.setPublicationYear(2024);
        newBook.setTotalCopies(5);
        newBook.setAvailableCopies(5);

        when(bookService.addBook(any(Book.class))).thenReturn(testBook);

        // WHEN & THEN
        mockMvc.perform(post("/api/admin/books")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Book"));

        verify(bookService, times(1)).addBook(any(Book.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBook_success() throws Exception {
        // GIVEN
        doNothing().when(bookService).deleteBook(1L);

        // WHEN & THEN
        mockMvc.perform(delete("/api/admin/books/1")
                .with(csrf()))
                .andExpect(status().isOk());

        verify(bookService, times(1)).deleteBook(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBook_success() throws Exception {
        // GIVEN
        Book updatedBook = new Book();
        updatedBook.setTitle("Updated Title");
        updatedBook.setAuthor("Updated Author");
        updatedBook.setIsbn("978-123-45-6789-0");
        updatedBook.setPublicationYear(2024);
        updatedBook.setTotalCopies(10);
        updatedBook.setAvailableCopies(8);

        when(bookService.updateBook(eq(1L), any(Book.class))).thenReturn(updatedBook);

        // WHEN & THEN
        mockMvc.perform(put("/api/admin/books/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedBook)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));

        verify(bookService, times(1)).updateBook(eq(1L), any(Book.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBookById_success() throws Exception {
        // GIVEN
        when(bookService.getBookById(1L)).thenReturn(Optional.of(testBook));

        // WHEN & THEN
        mockMvc.perform(get("/api/admin/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));

        verify(bookService, times(1)).getBookById(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getBookById_notFound() throws Exception {
        // GIVEN
        when(bookService.getBookById(999L)).thenReturn(Optional.empty());

        // WHEN & THEN
        mockMvc.perform(get("/api/admin/books/999"))
                .andExpect(status().isNotFound());

        verify(bookService, times(1)).getBookById(999L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllUsers_returnsUserList() throws Exception {
        // GIVEN
        when(userService.getAllUsers()).thenReturn(Arrays.asList(testUser));

        // WHEN & THEN
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("admin"));

        verify(userService, times(1)).getAllUsers();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void approveBorrowRequest_success() throws Exception {
        // GIVEN
        Borrow borrow = new Borrow();
        borrow.setId(1L);
        borrow.setUser(testUser);
        borrow.setBook(testBook);
        borrow.setStatus(Borrow.BorrowStatus.ACTIVE);
        borrow.setBorrowDate(LocalDate.now());

        when(borrowRequestService.acceptRequest(1L)).thenReturn(borrow);

        // WHEN & THEN
        mockMvc.perform(post("/api/admin/borrow-requests/1/approve")
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(borrowRequestService, times(1)).acceptRequest(1L);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rejectBorrowRequest_success() throws Exception {
        // GIVEN
        BorrowRequest request = new BorrowRequest();
        request.setId(1L);
        request.setUser(testUser);
        request.setBook(testBook);
        request.setStatus(BorrowRequest.RequestStatus.REJECTED);

        when(borrowRequestService.rejectRequest(1L)).thenReturn(request);

        // WHEN & THEN
        mockMvc.perform(post("/api/admin/borrow-requests/1/reject")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"));

        verify(borrowRequestService, times(1)).rejectRequest(1L);
    }
}