package com.library.libman.controller;

import com.library.libman.entity.Book;
import com.library.libman.entity.Borrow;
import com.library.libman.entity.User;
import com.library.libman.service.BookService;
import com.library.libman.service.BorrowService;
import com.library.libman.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "ahmet", roles = { "USER" })
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @MockBean
    private BorrowService borrowService;

    @MockBean
    private UserService userService;

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

        testBorrow = new Borrow();
        testBorrow.setId(100L);
        testBorrow.setUser(testUser);
        testBorrow.setBook(testBook);
        testBorrow.setBorrowDate(LocalDate.now());
        testBorrow.setStatus(Borrow.BorrowStatus.ACTIVE);
    }

    /**
     * GET /api/user/books
     * Kullanıcıya ödünç alınabilir kitapların listesini döndürmeli.
     */
    @Test
    void getAvailableBooks_returnsOkAndBookList() throws Exception {
        when(bookService.getAvailableBooks()).thenReturn(List.of(testBook));

        mockMvc.perform(get("/api/user/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testBook.getId()))
                .andExpect(jsonPath("$[0].title").value("Test Book"));

        verify(bookService, times(1)).getAvailableBooks();
    }

    /**
     * GET /api/user/borrows/username/{username}
     * Kullanıcı adı ile borrow geçmişini döndürmeli.
     */
    @Test
    void getUserBorrowsByUsername_success_returnsBorrowList() throws Exception {
        when(userService.getUserByUsername("ahmet")).thenReturn(testUser);
        when(borrowService.getUserBorrows(1L)).thenReturn(List.of(testBorrow));

        mockMvc.perform(get("/api/user/borrows/username/{username}", "ahmet"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(testBorrow.getId()));

        verify(userService, times(1)).getUserByUsername("ahmet");
        verify(borrowService, times(1)).getUserBorrows(1L);
    }
}