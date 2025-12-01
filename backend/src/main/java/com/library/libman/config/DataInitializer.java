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

        if (    (userRepository.findByUsername("duygu").isEmpty())&&
                (userRepository.findByUsername("ahmet").isEmpty())&&
                (userRepository.findByUsername("fatih").isEmpty())&&
                (userRepository.findByUsername("kaan").isEmpty())) {
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
        User admin1 = new User();
        admin1.setUsername("ahmet");
        admin1.setPassword(passwordEncoder.encode("ahmet123"));
        admin1.setEmail("ahmet@library.com");
        admin1.setFullName("Ahmet Taha ÖZCAN");
        admin1.setRole(User.UserRole.ADMIN);
        userRepository.save(admin1);

        // --- ADMIN 2 ---
        User admin2 = new User();
        admin2.setUsername("duygu");
        admin2.setPassword(passwordEncoder.encode("duygu123")); // Şifre: sifre2
        admin2.setEmail("duygu@library.com");
        admin2.setFullName("Duygu AKMAN");
        admin2.setRole(User.UserRole.ADMIN);
        userRepository.save(admin2);

        // --- ADMIN 3 ---
        User admin3 = new User();
        admin3.setUsername("kaan");
        admin3.setPassword(passwordEncoder.encode("kaan123")); // Şifre: sifre3
        admin3.setEmail("kaan@library.com");
        admin3.setFullName("Kaan BEHZETOĞLU");
        admin3.setRole(User.UserRole.ADMIN);
        userRepository.save(admin3);

        // --- ADMIN 4 ---
        User admin4 = new User();
        admin4.setUsername("fatih");
        admin4.setPassword(passwordEncoder.encode("fatih123")); // Şifre: sifre4
        admin4.setEmail("fatih@library.com");
        admin4.setFullName("Mehmet Fatih AKAY");
        admin4.setRole(User.UserRole.ADMIN);
        userRepository.save(admin4);

        System.out.println("✅ 4 Adet Admin kullanıcısı güvenli şifrelerle oluşturuldu.");
    }
}