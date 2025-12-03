package com.library.libman.config;

import com.library.libman.entity.Book;
import com.library.libman.entity.User;
import com.library.libman.repository.BookRepository;
import com.library.libman.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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
        if (bookRepository.count() == 0) {
            addTestBooks();
        }

        if (    (userRepository.findByUsername("duygu").isEmpty())&&
                (userRepository.findByUsername("ahmet").isEmpty())&&
                (userRepository.findByUsername("fatih").isEmpty())&&
                (userRepository.findByUsername("kaan").isEmpty())&&
                (userRepository.findByUsername("testuser").isEmpty())) {
            addTestUsers();
        }

        System.out.println("✅ Test verileri yüklendi!");
        System.out.println("📚 Kitap sayısı: " + bookRepository.count());
        System.out.println("👥 Kullanıcı sayısı: " + userRepository.count());
    }

    private void addTestBooks() {
        Random random = new Random();
        
        // 200 kitap yüklemesi
        String[][] books = {
            {"Suç ve Ceza", "Fyodor Dostoyevski", "1866"},
            {"Savaş ve Barış", "Lev Tolstoy", "1869"},
            {"Anna Karenina", "Lev Tolstoy", "1877"},
            {"1984", "George Orwell", "1949"},
            {"Hayvan Çiftliği", "George Orwell", "1945"},
            {"Bülbülü Öldürmek", "Harper Lee", "1960"},
            {"Yüzüklerin Efendisi", "J.R.R. Tolkien", "1954"},
            {"Harry Potter ve Felsefe Taşı", "J.K. Rowling", "1997"},
            {"Simyacı", "Paulo Coelho", "1988"},
            {"Tutunamayanlar", "Oğuz Atay", "1971"},
            {"Kürk Mantolu Madonna", "Sabahattin Ali", "1943"},
            {"İnce Memed", "Yaşar Kemal", "1955"},
            {"Beyaz Kale", "Orhan Pamuk", "1985"},
            {"Masumiyet Müzesi", "Orhan Pamuk", "2008"},
            {"Kara Kitap", "Orhan Pamuk", "1990"},
            {"Benim Adım Kırmızı", "Orhan Pamuk", "1998"},
            {"Kar", "Orhan Pamuk", "2002"},
            {"Yeni Hayat", "Orhan Pamuk", "1994"},
            {"Sessiz Ev", "Orhan Pamuk", "1983"},
            {"Cevdet Bey ve Oğulları", "Orhan Pamuk", "1982"},
            {"Don Kişot", "Miguel de Cervantes", "1605"},
            {"Moby Dick", "Herman Melville", "1851"},
            {"Odysseia", "Homeros", "800"},
            {"Dönüşüm", "Franz Kafka", "1915"},
            {"Dava", "Franz Kafka", "1925"},
            {"Şato", "Franz Kafka", "1926"},
            {"Budala", "Fyodor Dostoyevski", "1869"},
            {"Karamazov Kardeşler", "Fyodor Dostoyevski", "1880"},
            {"Ölü Canlar", "Nikolay Gogol", "1842"},
            {"Babalar ve Oğullar", "Ivan Turgenev", "1862"},
            {"Oblomov", "Ivan Goncharov", "1859"},
            {"Doktor Jivago", "Boris Pasternak", "1957"},
            {"Ustam ve Margarita", "Mihail Bulgakov", "1967"},
            {"Sakin Akan Don", "Mihail Şolohov", "1940"},
            {"Yabancı", "Albert Camus", "1942"},
            {"Veba", "Albert Camus", "1947"},
            {"Sisifos Söyleni", "Albert Camus", "1942"},
            {"Bulantı", "Jean-Paul Sartre", "1938"},
            {"İnsanın Anlam Arayışı", "Viktor Frankl", "1946"},
            {"Sefiller", "Victor Hugo", "1862"},
            {"Notre Dame'ın Kamburu", "Victor Hugo", "1831"},
            {"Madame Bovary", "Gustave Flaubert", "1856"},
            {"Kırmızı ve Siyah", "Stendhal", "1830"},
            {"Monte Cristo Kontu", "Alexandre Dumas", "1844"},
            {"Üç Silahşörler", "Alexandre Dumas", "1844"},
            {"Görünmez Adam", "H.G. Wells", "1897"},
            {"Zaman Makinesi", "H.G. Wells", "1895"},
            {"Dünyalar Savaşı", "H.G. Wells", "1898"},
            {"Frankenstein", "Mary Shelley", "1818"},
            {"Drakula", "Bram Stoker", "1897"},
            {"Robinson Crusoe", "Daniel Defoe", "1719"},
            {"Gulliver'in Gezileri", "Jonathan Swift", "1726"},
            {"Hazine Adası", "Robert Louis Stevenson", "1883"},
            {"Oliver Twist", "Charles Dickens", "1838"},
            {"David Copperfield", "Charles Dickens", "1850"},
            {"Büyük Umutlar", "Charles Dickens", "1861"},
            {"İki Şehrin Hikayesi", "Charles Dickens", "1859"},
            {"Gurur ve Önyargı", "Jane Austen", "1813"},
            {"Emma", "Jane Austen", "1815"},
            {"Akıl ve Duygu", "Jane Austen", "1811"},
            {"Jane Eyre", "Charlotte Brontë", "1847"},
            {"Uğultulu Tepeler", "Emily Brontë", "1847"},
            {"Fahrenheit 451", "Ray Bradbury", "1953"},
            {"Cesur Yeni Dünya", "Aldous Huxley", "1932"},
            {"Yaşlı Adam ve Deniz", "Ernest Hemingway", "1952"},
            {"Silahlar ve İnsanlar", "Ernest Hemingway", "1929"},
            {"Çanlar Kimin İçin Çalıyor", "Ernest Hemingway", "1940"},
            {"Güneş de Doğar", "Ernest Hemingway", "1926"},
            {"Muhteşem Gatsby", "F. Scott Fitzgerald", "1925"},
            {"Küçük Prens", "Antoine de Saint-Exupéry", "1943"},
            {"Demir Ökçe", "Jack London", "1907"},
            {"Beyaz Diş", "Jack London", "1906"},
            {"Martin Eden", "Jack London", "1909"},
            {"Çavdar Tarlasında Çocuklar", "J.D. Salinger", "1951"},
            {"Kayıp Zamanın İzinde", "Marcel Proust", "1913"},
            {"Otomatik Portakal", "Anthony Burgess", "1962"},
            {"Parçalanma", "Chinua Achebe", "1958"},
            {"Yüz Yıllık Yalnızlık", "Gabriel García Márquez", "1967"},
            {"Kolera Günlerinde Aşk", "Gabriel García Márquez", "1985"},
            {"Pedro Páramo", "Juan Rulfo", "1955"},
            {"Ficciones", "Jorge Luis Borges", "1944"},
            {"Aleph", "Jorge Luis Borges", "1949"},
            {"Yeraltından Notlar", "Fyodor Dostoyevski", "1864"},
            {"Beyaz Geceler", "Fyodor Dostoyevski", "1848"},
            {"Kumarbaz", "Fyodor Dostoyevski", "1867"},
            {"Satranç", "Stefan Zweig", "1942"},
            {"Bilinmeyen Bir Kadının Mektubu", "Stefan Zweig", "1922"},
            {"Olağanüstü Bir Gece", "Stefan Zweig", "1922"},
            {"Amok Koşucusu", "Stefan Zweig", "1922"},
            {"Korku", "Stefan Zweig", "1925"},
            {"Sineklerin Tanrısı", "William Golding", "1954"},
            {"Narnia Günlükleri", "C.S. Lewis", "1950"},
            {"Hobbit", "J.R.R. Tolkien", "1937"},
            {"Silmarillion", "J.R.R. Tolkien", "1977"},
            {"Kumlar Gezegeni", "Frank Herbert", "1965"},
            {"Vakıf", "Isaac Asimov", "1951"},
            {"Ben Robot", "Isaac Asimov", "1950"},
            {"Nöromancer", "William Gibson", "1984"},
            {"Kar Kazası", "Neal Stephenson", "1992"},
            {"Damızlık Kızın Öyküsü", "Margaret Atwood", "1985"},
            {"Oryx ve Crake", "Margaret Atwood", "2003"},
            {"Yol", "Cormac McCarthy", "2006"},
            {"Kan Meridyeni", "Cormac McCarthy", "1985"},
            {"İhtiyarlar İçin Yer Yok", "Cormac McCarthy", "2005"},
            {"Yolda", "Jack Kerouac", "1957"},
            {"Dharma Serserileri", "Jack Kerouac", "1958"},
            {"Çıplak Öğle", "William S. Burroughs", "1959"},
            {"Mezbaha Beş", "Kurt Vonnegut", "1969"},
            {"Kedi Beşiği", "Kurt Vonnegut", "1963"},
            {"Şampiyonların Kahvaltısı", "Kurt Vonnegut", "1973"},
            {"Gazap Üzümleri", "John Steinbeck", "1939"},
            {"Fareler ve İnsanlar", "John Steinbeck", "1937"},
            {"Eden'in Doğusu", "John Steinbeck", "1952"},
            {"İnci", "John Steinbeck", "1947"},
            {"Parıltı", "Stephen King", "1977"},
            {"O", "Stephen King", "1986"},
            {"Mahşer", "Stephen King", "1978"},
            {"Carrie", "Stephen King", "1974"},
            {"Sefalet", "Stephen King", "1987"},
            {"Hayvan Mezarlığı", "Stephen King", "1983"},
            {"Kara Kule", "Stephen King", "1982"},
            {"22 Kasım 1963", "Stephen King", "2011"},
            {"Yeşil Yol", "Stephen King", "1996"},
            {"Salem'e Gideceksin", "Stephen King", "1975"},
            {"Ölü Bölge", "Stephen King", "1979"},
            {"Christine", "Stephen King", "1983"},
            {"Cujo", "Stephen King", "1981"},
            {"Da Vinci Şifresi", "Dan Brown", "2003"},
            {"Melekler ve Şeytanlar", "Dan Brown", "2000"},
            {"Kayıp Sembol", "Dan Brown", "2009"},
            {"Cehennem", "Dan Brown", "2013"},
            {"Köken", "Dan Brown", "2017"},
            {"Veronika Ölmek İstiyor", "Paulo Coelho", "1998"},
            {"On Bir Dakika", "Paulo Coelho", "2003"},
            {"Zahir", "Paulo Coelho", "2005"},
            {"Brida", "Paulo Coelho", "1990"},
            {"Hac", "Paulo Coelho", "1987"},
            {"Piedra Irmağının Kıyısında Oturdum Ağladım", "Paulo Coelho", "1994"},
            {"Beşinci Dağ", "Paulo Coelho", "1996"},
            {"Şeytan ve Genç Kadın", "Paulo Coelho", "2000"},
            {"Portobello Cadısı", "Paulo Coelho", "2006"},
            {"Harry Potter ve Sırlar Odası", "J.K. Rowling", "1998"},
            {"Harry Potter ve Azkaban Tutsağı", "J.K. Rowling", "1999"},
            {"Harry Potter ve Ateş Kadehi", "J.K. Rowling", "2000"},
            {"Harry Potter ve Zümrüdüanka Yoldaşlığı", "J.K. Rowling", "2003"},
            {"Harry Potter ve Melez Prens", "J.K. Rowling", "2005"},
            {"Harry Potter ve Ölüm Yadigârları", "J.K. Rowling", "2007"},
            {"Açlık Oyunları", "Suzanne Collins", "2008"},
            {"Ateşi Yakalamak", "Suzanne Collins", "2009"},
            {"Alaycı Kuş", "Suzanne Collins", "2010"},
            {"Alacakaranlık", "Stephenie Meyer", "2005"},
            {"Yeni Ay", "Stephenie Meyer", "2006"},
            {"Tutulma", "Stephenie Meyer", "2007"},
            {"Şafak Vakti", "Stephenie Meyer", "2008"},
            {"Aynı Yıldızın Altında", "John Green", "2012"},
            {"Alaska'yı Aramak", "John Green", "2005"},
            {"Kağıt Kentler", "John Green", "2008"},
            {"Katherine'lerin Bolluğu", "John Green", "2006"},
            {"Sonsuza Kadar Kaplumbağalar", "John Green", "2017"},
            {"Bir Duvar Çiçeğinin Avantajları", "Stephen Chbosky", "1999"},
            {"Kayıp Kız", "Gillian Flynn", "2012"},
            {"Keskin Nesneler", "Gillian Flynn", "2006"},
            {"Karanlık Yerler", "Gillian Flynn", "2009"},
            {"Ejderha Dövmeli Kız", "Stieg Larsson", "2005"},
            {"Ateşle Oynayan Kız", "Stieg Larsson", "2006"},
            {"Hava Sarayı", "Stieg Larsson", "2007"},
            {"Uçurtma Avcısı", "Khaled Hosseini", "2003"},
            {"Bin Muhteşem Güneş", "Khaled Hosseini", "2007"},
            {"Dağların Yankısı", "Khaled Hosseini", "2013"},
            {"Pi'nin Yaşamı", "Yann Martel", "2001"},
            {"Kitap Hırsızı", "Markus Zusak", "2005"},
            {"Gece Yarısı Vakti", "Mark Haddon", "2003"},
            {"Zaman Gezgininin Karısı", "Audrey Niffenegger", "2003"},
            {"Filler İçin Su", "Sara Gruen", "2006"},
            {"Hizmetçiler", "Kathryn Stockett", "2009"},
            {"Sevgili Kemiklerim", "Alice Sebold", "2002"},
            {"Bir Geyşanın Anıları", "Arthur Golden", "1997"},
            {"Arıların Gizli Yaşamı", "Sue Monk Kidd", "2002"},
            {"Ye Dua Et Sev", "Elizabeth Gilbert", "2006"},
            {"Yaban", "Cheryl Strayed", "2012"},
            {"Eğitimli", "Tara Westover", "2018"},
            {"Oluş", "Michelle Obama", "2018"},
            {"Sapiens", "Yuval Noah Harari", "2011"},
            {"Homo Deus", "Yuval Noah Harari", "2015"},
            {"21. Yüzyıl İçin 21 Ders", "Yuval Noah Harari", "2018"},
            {"Mai ve Siyah", "Halit Ziya Uşaklıgil", "1897"},
            {"Aşk-ı Memnu", "Halit Ziya Uşaklıgil", "1899"},
            {"Ateşten Gömlek", "Halide Edib Adıvar", "1922"},
            {"Sinekli Bakkal", "Halide Edib Adıvar", "1936"},
            {"Çalıkuşu", "Reşat Nuri Güntekin", "1922"},
            {"Yaprak Dökümü", "Reşat Nuri Güntekin", "1930"},
            {"Eylül", "Mehmet Rauf", "1901"},
            {"Huzur", "Ahmet Hamdi Tanpınar", "1949"},
            {"Saatleri Ayarlama Enstitüsü", "Ahmet Hamdi Tanpınar", "1954"},
            {"Araba Sevdası", "Recaizade Mahmut Ekrem", "1896"},
            {"Kiralık Konak", "Yakup Kadri Karaosmanoğlu", "1922"},
            {"Sodom ve Gomore", "Yakup Kadri Karaosmanoğlu", "1928"},
            {"Yaban", "Yakup Kadri Karaosmanoğlu", "1932"},
            {"Sanat ve İsyan", "Albert Camus", "1951"},
            {"Düşüş", "Albert Camus", "1956"}
        };
        
        for (int i = 0; i < books.length; i++) {
            Book book = new Book();
            book.setTitle(books[i][0]);
            book.setAuthor(books[i][1]);
            // ISBN yükle
            book.setIsbn(String.format("978-%03d-%02d-%04d-%d", 
                (100 + i), (i % 100), (1000 + i), (i % 10)));
            book.setPublicationYear(Integer.parseInt(books[i][2]));
            
            // copy sayisı
            int copies = 1 + random.nextInt(100);
            book.setTotalCopies(copies);
            book.setAvailableCopies(copies);
            
            bookRepository.save(book);
        }

        System.out.println("📖 " + books.length + " farklı gerçek kitap eklendi!");
    }

    private void addTestUsers() {
        User admin1 = new User();
        admin1.setUsername("ahmet");
        admin1.setPassword(passwordEncoder.encode("ahmet123"));
        admin1.setEmail("ahmet@library.com");
        admin1.setFullName("Ahmet Taha ÖZCAN");
        admin1.setRole(User.UserRole.ADMIN);
        userRepository.save(admin1);

        // 2
        User admin2 = new User();
        admin2.setUsername("duygu");
        admin2.setPassword(passwordEncoder.encode("duygu123")); // Şifre: sifre2
        admin2.setEmail("duygu@library.com");
        admin2.setFullName("Duygu AKMAN");
        admin2.setRole(User.UserRole.ADMIN);
        userRepository.save(admin2);

        // 3
        User admin3 = new User();
        admin3.setUsername("kaan");
        admin3.setPassword(passwordEncoder.encode("kaan123")); // Şifre: sifre3
        admin3.setEmail("kaan@library.com");
        admin3.setFullName("Kaan BEHZETOĞLU");
        admin3.setRole(User.UserRole.ADMIN);
        userRepository.save(admin3);

        // 4
        User admin4 = new User();
        admin4.setUsername("fatih");
        admin4.setPassword(passwordEncoder.encode("fatih123")); // Şifre: sifre4
        admin4.setEmail("fatih@library.com");
        admin4.setFullName("Mehmet Fatih AKAY");
        admin4.setRole(User.UserRole.ADMIN);
        userRepository.save(admin4);

        // genel
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword(passwordEncoder.encode("test123"));
        testUser.setEmail("testuser@library.com");
        testUser.setFullName("Test Kullanıcı");
        testUser.setRole(User.UserRole.USER);
        userRepository.save(testUser);

        System.out.println("✅ 4 Adet Admin + 1 Test kullanıcısı oluşturuldu.");
    }
}