package com.library.libman.controller;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "ahmet", roles = { "USER" })
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private BorrowService borrowService;

    @MockBean
    private UserService userService;

    @MockBean
    private BorrowRequestService borrowRequestService;

    @MockBean
    private ProfileUpdateRequestService profileUpdateRequestService;

    private Book testBook;
    private User testUser;
    private Borrow testBorrow;

    @BeforeEach
    void setUp() {
        testBook = new Book();
        testBook.setId(10L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setPublicationYear(2024);
        testBook.setTotalCopies(5);
        testBook.setAvailableCopies(3);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("ahmet");
        testUser.setEmail("ahmet@test.com");
        testUser.setFullName("Ahmet Test");
        testUser.setRole(User.UserRole.USER);

        testBorrow = new Borrow();
        testBorrow.setId(100L);
        testBorrow.setUser(testUser);
        testBorrow.setBook(testBook);
        testBorrow.setBorrowDate(LocalDate.now());
        testBorrow.setStatus(Borrow.BorrowStatus.ACTIVE);
    }

    // ============================================
    // KİTAP LİSTELEME TESTLERİ
    // ============================================

    @Test
    void getAllBooks_returnsOkAndBookList() throws Exception {
        // GIVEN
        when(bookService.getAllBooks()).thenReturn(List.of(testBook));

        // WHEN & THEN
        mockMvc.perform(get("/api/user/books/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testBook.getId()))
                .andExpect(jsonPath("$[0].title").value("Test Book"));

        verify(bookService, times(1)).getAllBooks();
    }

    @Test
    void getBooksByTitle_returnsMatchingBooks() throws Exception {
        // GIVEN
        when(bookService.getBooksByTitle("Test")).thenReturn(List.of(testBook));

        // WHEN & THEN
        mockMvc.perform(get("/api/user/books/title/{title}", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Book"));

        verify(bookService, times(1)).getBooksByTitle("Test");
    }

    // ============================================
    // ÖDÜNÇ ALMA TESTLERİ
    // ============================================

    @Test
    void getUserBorrowsByUsername_success_returnsBorrowList() throws Exception {
        // GIVEN
        when(userService.getUserByUsername("ahmet")).thenReturn(testUser);
        when(borrowService.getUserBorrows(1L)).thenReturn(List.of(testBorrow));

        // WHEN & THEN
        mockMvc.perform(get("/api/user/borrows/username/{username}", "ahmet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testBorrow.getId()));

        verify(userService, times(1)).getUserByUsername("ahmet");
        verify(borrowService, times(1)).getUserBorrows(1L);
    }

    @Test
    void returnBookByUsername_success() throws Exception {
        // GIVEN
        Borrow returnedBorrow = new Borrow();
        returnedBorrow.setId(100L);
        returnedBorrow.setStatus(Borrow.BorrowStatus.RETURNED);
        returnedBorrow.setReturnDate(LocalDate.now());

        when(borrowService.returnBook(100L)).thenReturn(returnedBorrow);

        // WHEN & THEN
        mockMvc.perform(post("/api/user/return/username/{username}/{borrowId}", "ahmet", 100L)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETURNED"));

        verify(borrowService, times(1)).returnBook(100L);
    }

    @Test
    void returnBookByUsername_throws_whenAlreadyReturned() throws Exception {
        // GIVEN
        when(borrowService.returnBook(100L))
                .thenThrow(new RuntimeException("Bu kitap zaten iade edilmiş"));

        // WHEN & THEN
        mockMvc.perform(post("/api/user/return/username/{username}/{borrowId}", "ahmet", 100L)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Bu kitap zaten iade edilmiş"));

        verify(borrowService, times(1)).returnBook(100L);
    }

    // ============================================
    // ÖDÜNÇ İSTEĞİ TESTLERİ
    // ============================================

    @Test
    void requestBook_success() throws Exception {
        // GIVEN
        when(userService.getUserByUsername("ahmet")).thenReturn(testUser);
        when(borrowRequestService.borrowRequestBook(1L, 10L)).thenReturn(new com.library.libman.entity.BorrowRequest());

        // WHEN & THEN
        mockMvc.perform(post("/api/user/borrow-request/{username}/{bookId}", "ahmet", 10L)
                .with(csrf()))
                .andExpect(status().isCreated());

        verify(borrowRequestService, times(1)).borrowRequestBook(1L, 10L);
    }

    @Test
    void requestBook_throws_whenBookNotAvailable() throws Exception {
        // GIVEN
        when(userService.getUserByUsername("ahmet")).thenReturn(testUser);
        when(borrowRequestService.borrowRequestBook(1L, 10L))
                .thenThrow(new RuntimeException("Kitap şu anda mevcut değil"));

        // WHEN & THEN
        mockMvc.perform(post("/api/user/borrow-request/{username}/{bookId}", "ahmet", 10L)
                .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Kitap şu anda mevcut değil"));

        verify(borrowRequestService, times(1)).borrowRequestBook(1L, 10L);
    }

    @Test
    void getUserRequestByUsername_returnsOk() throws Exception {
        // GIVEN
        when(userService.getUserByUsername("ahmet")).thenReturn(testUser);
        when(borrowRequestService.getUserBorrowRequests(1L)).thenReturn(List.of());

        // WHEN & THEN
        mockMvc.perform(get("/api/user/{username}/borrow-requests", "ahmet"))
                .andExpect(status().isOk());

        verify(borrowRequestService, times(1)).getUserBorrowRequests(1L);
    }

    // ============================================
    // PROFİL GÜNCELLEME TESTLERİ
    // ============================================

    @Test
    void getMe_returnsCurrentUserWithoutPassword() throws Exception {
        // GIVEN
        when(userService.getUserByUsername("ahmet")).thenReturn(testUser);

        // WHEN & THEN
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ahmet"))
                .andExpect(jsonPath("$.email").value("ahmet@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService, times(1)).getUserByUsername("ahmet");
    }

    @Test
    void requestProfileUpdate_success() throws Exception {
        // GIVEN
        User updateRequest = new User();
        updateRequest.setUsername("newahmet");
        updateRequest.setEmail("newahmet@test.com");

        when(userService.getUserByUsername("ahmet")).thenReturn(testUser);
        when(profileUpdateRequestService.createRequest(eq(1L), eq("newahmet"), eq("newahmet@test.com")))
                .thenReturn(new com.library.libman.entity.ProfileUpdateRequest());

        // WHEN & THEN
        mockMvc.perform(post("/api/user/settings/profile-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("İstek gönderildi."));

        verify(profileUpdateRequestService, times(1)).createRequest(1L, "newahmet", "newahmet@test.com");
    }

    @Test
    void requestProfileUpdate_throws_whenEmailAlreadyUsed() throws Exception {
        // GIVEN
        User updateRequest = new User();
        updateRequest.setUsername("newahmet");
        updateRequest.setEmail("taken@test.com");

        when(userService.getUserByUsername("ahmet")).thenReturn(testUser);
        when(profileUpdateRequestService.createRequest(eq(1L), eq("newahmet"), eq("taken@test.com")))
                .thenThrow(new RuntimeException("E-posta kullanımda."));

        // WHEN & THEN
        mockMvc.perform(post("/api/user/settings/profile-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("E-posta kullanımda."));

        verify(profileUpdateRequestService, times(1)).createRequest(1L, "newahmet", "taken@test.com");
    }

    @Test
    void requestProfileUpdate_throws_whenUsernameAlreadyUsed() throws Exception {
        // GIVEN
        User updateRequest = new User();
        updateRequest.setUsername("takenuser");
        updateRequest.setEmail("newemail@test.com");

        when(userService.getUserByUsername("ahmet")).thenReturn(testUser);
        when(profileUpdateRequestService.createRequest(eq(1L), eq("takenuser"), eq("newemail@test.com")))
                .thenThrow(new RuntimeException("Kullanıcı adı kullanımda."));

        // WHEN & THEN
        mockMvc.perform(post("/api/user/settings/profile-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Kullanıcı adı kullanımda."));

        verify(profileUpdateRequestService, times(1)).createRequest(1L, "takenuser", "newemail@test.com");
    }

    @Test
    @WithMockUser(username = "otheruser", roles = { "USER" })
    void requestProfileUpdate_usesAuthenticatedUser() throws Exception {
        // GIVEN
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("otheruser");

        User updateRequest = new User();
        updateRequest.setUsername("updateduser");
        updateRequest.setEmail("updated@test.com");

        when(userService.getUserByUsername("otheruser")).thenReturn(otherUser);
        when(profileUpdateRequestService.createRequest(eq(2L), eq("updateduser"), eq("updated@test.com")))
                .thenReturn(new com.library.libman.entity.ProfileUpdateRequest());

        // WHEN & THEN
        mockMvc.perform(post("/api/user/settings/profile-request")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUserByUsername("otheruser");
        verify(profileUpdateRequestService, times(1)).createRequest(2L, "updateduser", "updated@test.com");
    }
}