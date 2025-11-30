package com.library.libman.config;

import com.library.libman.entity.Book;
import com.library.libman.entity.User;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
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
        // Kitap ekleme kodları...
        Book book1 = new Book();
        book1.setTitle("Suç ve Ceza");
        book1.setAuthor("Fyodor Dostoyevski");
        book1.setIsbn("978-975-08-1234-5");
        book1.setPublicationYear(1866);
        book1.setTotalCopies(5);
        book1.setAvailableCopies(5);
        bookRepository.save(book1);

        // ... diğer kitaplar ...

        System.out.println("📖 Test kitapları eklendi");
    }

    private void addTestUsers() {
        // ADMIN
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123")); // Şifrelenmiş
        admin.setEmail("admin@library.com");
        admin.setFullName("Yönetici Admin");
        admin.setRole(User.UserRole.ADMIN);
        userRepository.save(admin);

        // USER 1
        User user1 = new User();
        user1.setUsername("ahmet");
        user1.setPassword(passwordEncoder.encode("ahmet123")); // Şifrelenmiş
        user1.setEmail("ahmet@example.com");
        user1.setFullName("Ahmet Yılmaz");
        user1.setRole(User.UserRole.USER);
        userRepository.save(user1);

        // USER 2
        User user2 = new User();
        user2.setUsername("ayse");
        user2.setPassword(passwordEncoder.encode("ayse123")); // Şifrelenmiş
        user2.setEmail("ayse@example.com");
        user2.setFullName("Ayşe Demir");
        user2.setRole(User.UserRole.USER);
        userRepository.save(user2);

        System.out.println("👤 Test kullanıcıları eklendi (Şifreler şifrelendi)");
    }
}