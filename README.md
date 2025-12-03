# 📚 LibMan - Kütüphane Yönetim Sistemi

Kütüphane işlemlerini dijitalleştiren, kitap ve kullanıcı yönetimini kolaylaştıran web tabanlı bir uygulama.

## 🚀 Özellikler

### Admin Paneli
- Kitap ekleme, düzenleme ve silme
- Kullanıcı yönetimi
- Ödünç talepleri onaylama/reddetme
- Profil güncelleme taleplerini yönetme
- Tüm ödünç işlemlerini görüntüleme

### Kullanıcı Paneli
- Kitap arama ve listeleme
- Kitap ödünç alma talebi gönderme
- Ödünç aldığı kitapları görüntüleme
- Kitap iade etme
- Profil güncelleme talebi gönderme

## 🛠️ Teknolojiler

| Katman | Teknoloji |
|--------|-----------|
| Backend | Spring Boot 3.2.0 |
| Veritabanı | MySQL |
| Frontend | HTML, CSS, JavaScript |
| Güvenlik | Spring Security |
| Test | JUnit 5, Mockito, H2 (in-memory) |
| Coverage | JaCoCo |

## 📋 Gereksinimler

- Java 21
- Maven 3.6+
- MySQL 8.0+

## ⚙️ Kurulum

### 1. Projeyi Klonlayın
```bash
git clone https://github.com/username/LibMan.git
cd LibMan
```

### 2. MySQL Veritabanını Ayarlayın

MySQL'de `library_management` veritabanı manuel oluşturulur.

`backend/src/main/resources/application.properties` dosyasında MySQL bilgilerinizi güncelleyin:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/library_management?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Uygulamayı Çalıştırın
```bash
cd backend
mvn spring-boot:run
```

### 4. Tarayıcıda Açın
```
http://localhost:8080
```

## 👤 Varsayılan Kullanıcılar

Uygulama ilk çalıştırıldığında otomatik olarak oluşturulur:

### Admin Kullanıcıları
| Kullanıcı Adı | Şifre | Rol |
|---------------|-------|-----|
| ahmet | ahmet123 | ADMIN |
| duygu | duygu123 | ADMIN |
| kaan | kaan123 | ADMIN |
| fatih | fatih123 | ADMIN |

### Test Kullanıcısı
| Kullanıcı Adı | Şifre | Rol |
|---------------|-------|-----|
| testuser | test123 | USER |

## 📖 Örnek Veriler

Veritabanı boşken uygulama başlatıldığında **200 farklı Türkçe kitap** otomatik eklenir

## 🧪 Testler

### Testleri Çalıştırma
```bash
cd backend
mvn clean test
```

### Test Coverage Raporu
```bash
mvn test jacoco:report
```
Rapor: `backend/target/site/jacoco/index.html`

## 📁 Proje Yapısı

```
LibMan/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/library/libman/
│   │   │   │   ├── config/          # Güvenlik ve veri başlatma
│   │   │   │   ├── controller/      # REST API ve Web Controller
│   │   │   │   ├── entity/          # Veritabanı modelleri
│   │   │   │   ├── exception/       # Hata yönetimi
│   │   │   │   ├── repository/      # Veritabanı işlemleri
│   │   │   │   └── service/         # İş mantığı
│   │   │   └── resources/
│   │   │       ├── static/          # CSS, JS dosyaları
│   │   │       ├── templates/       # HTML şablonları
│   │   │       └── application.properties
│   │   └── test/                    # Unit ve Integration testler
│   └── pom.xml
└── documentation/                   # Proje dökümanları
```

## 🔗 API Endpoints

### Kimlik Doğrulama
| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/login` | Giriş sayfası |
| GET | `/register` | Kayıt sayfası |
| POST | `/register` | Yeni kullanıcı kaydı |

### Admin API (`/api/admin/*`)
| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/admin/books` | Tüm kitapları listele |
| POST | `/api/admin/books` | Yeni kitap ekle |
| PUT | `/api/admin/books/{id}` | Kitap güncelle |
| DELETE | `/api/admin/books/{id}` | Kitap sil |
| GET | `/api/admin/borrow-requests` | Ödünç taleplerini listele |
| POST | `/api/admin/borrow-requests/{id}/approve` | Talebi onayla |
| POST | `/api/admin/borrow-requests/{id}/reject` | Talebi reddet |

### User API (`/api/user/*`)
| Method | Endpoint | Açıklama |
|--------|----------|----------|
| GET | `/api/user/books` | Mevcut kitapları listele |
| POST | `/api/user/borrow-request` | Ödünç talebi gönder |
| GET | `/api/user/my-borrows` | Ödünç aldığım kitaplar |
| POST | `/api/user/return/{borrowId}` | Kitap iade et |

## 👥 Geliştiriciler

- Ahmet Taha ÖZCAN
- Duygu AKMAN
- Kaan BEHZETOĞLU
- Mehmet Fatih AKAY


Bu proje eğitim amaçlı geliştirilmiştir.
