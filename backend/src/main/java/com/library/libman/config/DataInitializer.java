package com.library.libman.config;

import com.library.libman.entity.Book;
import com.library.libman.entity.User;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
        System.out.println("🚀 DataInitializer Başlatıldı...");

        // KİTAPLARI EKLE
        if (bookRepository.count() == 0) {
            System.out.println("📦 Veritabanı boş, kitaplar ekleniyor...");
            try {
                addTestBooks();
            } catch (Exception e) {
                System.err.println("❌ Kitap ekleme sırasında hata: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ Veritabanında zaten kitaplar var. Ekleme atlandı.");
        }

        // KULLANICILARI EKLE
        if (userRepository.findByUsername("ahmet").isEmpty()) {
            System.out.println("👤 Admin kullanıcıları bulunamadı, oluşturuluyor...");
            addTestUsers();
        } else {
            System.out.println("⚠️ Admin kullanıcıları zaten mevcut. Ekleme atlandı.");
        }

        System.out.println("✅ Başlangıç işlemleri tamamlandı!");
    }

    private void addTestBooks() {
        Random random = new Random();
        List<Book> bookList = new ArrayList<>();
        int targetBookCount = 2000;

        String[][] realBooks = {
                {"Suç ve Ceza", "Fyodor Dostoyevski", "1866"},
                {"Savaş ve Barış", "Lev Tolstoy", "1869"},
                {"1984", "George Orwell", "1949"},
                {"Hayvan Çiftliği", "George Orwell", "1945"},
                {"Simyacı", "Paulo Coelho", "1988"},
                {"Dönüşüm", "Franz Kafka", "1915"},
                {"Sefiller", "Victor Hugo", "1862"},
                {"Yüzüklerin Efendisi", "J.R.R. Tolkien", "1954"}
        };

        String[] adjectives = {"Gizli", "Karanlık", "Mavi", "Sessiz", "Yalnız", "Ebedi", "Kırık", "Antik"};
        String[] nouns = {"Liman", "Şehir", "Gölge", "Deniz", "Nehir", "Dağ", "Orman", "Kale"};

        for (int i = 0; i < targetBookCount; i++) {
            Book book = new Book();

            String isbn = String.format("978-%03d-%02d-%04d-%d",
                    random.nextInt(1000),
                    random.nextInt(100),
                    i,
                    random.nextInt(10));

            book.setIsbn(isbn);
            book.setTotalCopies(1 + random.nextInt(50));
            book.setAvailableCopies(book.getTotalCopies());

            if (i < realBooks.length) {
                book.setTitle(realBooks[i][0]);
                book.setAuthor(realBooks[i][1]);
                book.setPublicationYear(Integer.parseInt(realBooks[i][2]));
            } else {
                String title = adjectives[random.nextInt(adjectives.length)] + " " + nouns[random.nextInt(nouns.length)];
                book.setTitle(title + " (No: " + (i+1) + ")");
                book.setAuthor("Yazar " + (i+1));
                book.setPublicationYear(1900 + random.nextInt(124));
            }
            bookList.add(book);
        }

        bookRepository.saveAll(bookList);
        System.out.println("📚 " + bookList.size() + " kitap başarıyla kaydedildi.");
    }

    private void addTestUsers() {
        createAdmin("ahmet", "ahmet123", "Ahmet Taha ÖZCAN");
        createAdmin("duygu", "duygu123", "Duygu AKMAN");
        createAdmin("kaan", "kaan123", "Kaan BEHZETOĞLU");
        createAdmin("fatih", "fatih123", "Mehmet Fatih AKAY");
        System.out.println("👥 Admin kullanıcıları oluşturuldu.");
    }

    private void createAdmin(String username, String rawPassword, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(username + "@library.com");
        user.setFullName(fullName);
        user.setRole(User.UserRole.ADMIN);
        userRepository.save(user);
    }
}