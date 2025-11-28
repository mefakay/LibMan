package com.library.libman.config;

import com.library.libman.entity.Book;
import com.library.libman.entity.User;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    
    @Autowired
    private BookRepository bookRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public void run(String... args) throws Exception {
        // Eğer veritabanı boşsa test verileri ekle
        if (bookRepository.count() == 0) {
            addTestBooks();
        }
        
        if (userRepository.count() == 0) {
            addTestUsers();
        }
        
        System.out.println("✅ Test verileri yüklendi!");
        System.out.println("📚 Kitap sayısı: " + bookRepository.count());
        System.out.println("👥 Kullanıcı sayısı: " + userRepository.count());
    }
    
    private void addTestBooks() {
        Book book1 = new Book();
        book1.setTitle("Suç ve Ceza");
        book1.setAuthor("Fyodor Dostoyevski");
        book1.setIsbn("978-975-08-1234-5");
        book1.setPublicationYear(1866);
        book1.setTotalCopies(5);
        book1.setAvailableCopies(5);
        bookRepository.save(book1);
        
        Book book2 = new Book();
        book2.setTitle("Savaş ve Barış");
        book2.setAuthor("Lev Tolstoy");
        book2.setIsbn("978-975-08-2345-6");
        book2.setPublicationYear(1869);
        book2.setTotalCopies(3);
        book2.setAvailableCopies(3);
        bookRepository.save(book2);
        
        Book book3 = new Book();
        book3.setTitle("1984");
        book3.setAuthor("George Orwell");
        book3.setIsbn("978-975-08-3456-7");
        book3.setPublicationYear(1949);
        book3.setTotalCopies(4);
        book3.setAvailableCopies(2); // 2 tanesi ödünç verilmiş gibi
        bookRepository.save(book3);
        
        System.out.println("📖 Test kitapları eklendi");
    }
    
    private void addTestUsers() {
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setEmail("admin@library.com");
        admin.setFullName("Yönetici Admin");
        admin.setRole(User.UserRole.ADMIN);
        userRepository.save(admin);
        
        User user1 = new User();
        user1.setUsername("ahmet");
        user1.setPassword("ahmet123");
        user1.setEmail("ahmet@example.com");
        user1.setFullName("Ahmet Yılmaz");
        user1.setRole(User.UserRole.USER);
        userRepository.save(user1);
        
        User user2 = new User();
        user2.setUsername("ayse");
        user2.setPassword("ayse123");
        user2.setEmail("ayse@example.com");
        user2.setFullName("Ayşe Demir");
        user2.setRole(User.UserRole.USER);
        userRepository.save(user2);
        
        System.out.println("👤 Test kullanıcıları eklendi");
    }
}


