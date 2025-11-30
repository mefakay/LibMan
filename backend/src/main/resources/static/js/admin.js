/* ============================================
   ADMIN SAYFASI JAVASCRIPT
   ============================================ */

// Tab yönetimi
function switchTab(tabName) {
    // Tüm tab'ları gizle
    document.querySelectorAll('.tab-content').forEach(content => {
        content.classList.remove('active');
    });
    
    // Tüm tab butonlarını pasif yap
    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.remove('active');
    });
    
    // Seçilen tab'ı göster
    document.getElementById(tabName).classList.add('active');
    event.target.classList.add('active');
}

// Alert göster (Toast notification kullanıyor)
function showAlert(message, isError = false) {
    const type = isError ? 'error' : 'success';
    showToast(message, type);
}

// ============================================
// KİTAP İŞLEMLERİ
// ============================================

async function getAllBooks() {
    try {
        const data = await apiGet('/admin/books');
        displayBooksTable(data);
    } catch (error) {
        showAlert('Kitaplar yüklenirken hata oluştu: ' + error.message, true);
    }
}

function displayBooksTable(books) {
    const container = document.getElementById('booksTable');
    if (!books || books.length === 0) {
        container.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Henüz kitap eklenmemiş.</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Başlık</th><th>Yazar</th><th>ISBN</th><th>Yıl</th><th>Toplam</th><th>Mevcut</th><th>Durum</th></tr></thead><tbody>';
    
    books.forEach(book => {
        const available = book.availableCopies > 0;
        html += `
            <tr>
                <td>${book.id}</td>
                <td><strong>${book.title}</strong></td>
                <td>${book.author}</td>
                <td>${book.isbn}</td>
                <td>${book.publicationYear || '-'}</td>
                <td>${book.totalCopies}</td>
                <td>${book.availableCopies}</td>
                <td><span class="badge ${available ? 'available' : 'unavailable'}">${available ? 'Mevcut' : 'Tükendi'}</span></td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

async function addBook() {
    const book = {
        title: document.getElementById('bookTitle').value,
        author: document.getElementById('bookAuthor').value,
        isbn: document.getElementById('bookIsbn').value,
        publicationYear: parseInt(document.getElementById('bookYear').value) || null,
        totalCopies: parseInt(document.getElementById('bookTotal').value) || 1,
        availableCopies: parseInt(document.getElementById('bookTotal').value) || 1
    };
    
    if (!book.title || !book.author || !book.isbn) {
        showAlert('Lütfen tüm zorunlu alanları doldurun!', true);
        return;
    }
    
    try {
        const data = await apiPost('/admin/books', book);
        showAlert('Kitap başarıyla eklendi!');
        document.getElementById('bookTitle').value = '';
        document.getElementById('bookAuthor').value = '';
        document.getElementById('bookIsbn').value = '';
        document.getElementById('bookYear').value = '';
        document.getElementById('bookTotal').value = '5';
        getAllBooks();
    } catch (error) {
        showAlert('Hata: ' + error.message, true);
    }
}

async function updateBook() {
    const bookId = document.getElementById('updateBookId').value;
    const book = {
        title: document.getElementById('updateTitle').value,
        author: document.getElementById('updateAuthor').value,
        isbn: document.getElementById('updateIsbn').value,
        publicationYear: parseInt(document.getElementById('updateYear').value) || null,
        totalCopies: parseInt(document.getElementById('updateTotal').value) || null,
        availableCopies: parseInt(document.getElementById('updateAvailable').value) || null
    };
    
    if (!bookId || !book.title || !book.author || !book.isbn || !book.totalCopies) {
        showAlert('Lütfen tüm zorunlu alanları doldurun!', true);
        return;
    }
    
    try {
        const data = await apiPut(`/admin/books/${bookId}`, book);
        showAlert('Kitap başarıyla güncellendi!');
        document.getElementById('updateBookId').value = '';
        document.getElementById('updateTitle').value = '';
        document.getElementById('updateAuthor').value = '';
        document.getElementById('updateIsbn').value = '';
        document.getElementById('updateYear').value = '';
        document.getElementById('updateTotal').value = '';
        document.getElementById('updateAvailable').value = '';
        getAllBooks();
    } catch (error) {
        showAlert('Hata: ' + error.message, true);
    }
}

async function deleteBook() {
    const bookId = document.getElementById('deleteBookId').value;
    if (!bookId) {
        showAlert('Lütfen kitap ID girin!', true);
        return;
    }
    
    if (!confirm('Bu kitabı silmek istediğinizden emin misiniz?')) {
        return;
    }
    
    try {
        await apiDelete(`/admin/books/${bookId}`);
        showAlert('Kitap başarıyla silindi!');
        document.getElementById('deleteBookId').value = '';
        getAllBooks();
    } catch (error) {
        showAlert('Hata: ' + error.message, true);
    }
}

// ============================================
// KULLANICI İŞLEMLERİ
// ============================================

async function getAllUsers() {
    try {
        const data = await apiGet('/admin/users');
        displayUsersTable(data);
    } catch (error) {
        showAlert('Kullanıcılar yüklenirken hata oluştu: ' + error.message, true);
    }
}

function displayUsersTable(users) {
    const container = document.getElementById('usersTable');
    if (!users || users.length === 0) {
        container.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Henüz kullanıcı eklenmemiş.</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Kullanıcı Adı</th><th>Ad Soyad</th><th>Email</th><th>Rol</th></tr></thead><tbody>';
    
    users.forEach(user => {
        html += `
            <tr>
                <td>${user.id}</td>
                <td><strong>${user.username}</strong></td>
                <td>${user.fullName}</td>
                <td>${user.email}</td>
                <td><span class="badge ${user.role}">${user.role}</span></td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

// ============================================
// ÖDÜNÇ İSTEKLERİ YÖNETİMİ
// ============================================

// Bekleyen istekleri getir
async function getPendingRequests() {
    try {
        const data = await apiGet('/admin/borrow-requests/pending');
        // Sadece PENDING olanları filtrele
        const pendingRequests = data.filter(r => r.status === 'PENDING');
        displayPendingRequestsTable(pendingRequests);
    } catch (error) {
        showAlert('Bekleyen istekler yüklenirken hata oluştu: ' + error.message, true);
    }
}

// Tüm istekleri getir
async function getAllRequests() {
    try {
        const data = await apiGet('/admin/borrow-requests/pending');
        displayAllRequestsTable(data);
    } catch (error) {
        showAlert('İstekler yüklenirken hata oluştu: ' + error.message, true);
    }
}

// Bekleyen istekleri tabloda göster (Onayla/Reddet butonlarıyla)
function displayPendingRequestsTable(requests) {
    const container = document.getElementById('pendingRequestsTable');
    if (!container) return;
    
    if (!requests || requests.length === 0) {
        container.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Bekleyen ödünç isteği yok.</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Kullanıcı</th><th>Kitap</th><th>İstek Tarihi</th><th>İşlemler</th></tr></thead><tbody>';
    
    requests.forEach(request => {
        html += `
            <tr>
                <td>${request.id}</td>
                <td><strong>${request.user.username}</strong><br><small>${request.user.fullName}</small></td>
                <td><strong>${request.book.title}</strong><br><small>${request.book.author}</small></td>
                <td>${request.requestDate}</td>
                <td>
                    <button class="success" onclick="approveRequest(${request.id})" style="margin-right: 5px; padding: 5px 10px; font-size: 12px;">✅ Onayla</button>
                    <button class="danger" onclick="rejectRequest(${request.id})" style="padding: 5px 10px; font-size: 12px;">❌ Reddet</button>
                </td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

// Tüm istekleri tabloda göster
function displayAllRequestsTable(requests) {
    const container = document.getElementById('allRequestsTable');
    if (!container) return;
    
    if (!requests || requests.length === 0) {
        container.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Henüz ödünç isteği yok.</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Kullanıcı</th><th>Kitap</th><th>İstek Tarihi</th><th>İşlem Tarihi</th><th>Durum</th></tr></thead><tbody>';
    
    requests.forEach(request => {
        let statusClass = '';
        let statusText = '';
        
        switch(request.status) {
            case 'PENDING':
                statusClass = 'pending';
                statusText = '⏳ Beklemede';
                break;
            case 'APPROVED':
                statusClass = 'available';
                statusText = '✅ Onaylandı';
                break;
            case 'REJECTED':
                statusClass = 'unavailable';
                statusText = '❌ Reddedildi';
                break;
            default:
                statusClass = '';
                statusText = request.status;
        }
        
        html += `
            <tr>
                <td>${request.id}</td>
                <td><strong>${request.user.username}</strong><br><small>${request.user.fullName}</small></td>
                <td><strong>${request.book.title}</strong><br><small>${request.book.author}</small></td>
                <td>${request.requestDate}</td>
                <td>${request.processedDate || '-'}</td>
                <td><span class="badge ${statusClass}">${statusText}</span></td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

// İsteği onayla
async function approveRequest(requestId) {
    if (!confirm('Bu ödünç isteğini onaylamak istediğinizden emin misiniz?')) {
        return;
    }
    
    try {
        await apiPost(`/admin/borrow-requests/${requestId}/approve`, {});
        showAlert('Ödünç isteği onaylandı! Kitap kullanıcıya verildi.');
        getPendingRequests();
        getAllRequests();
        getAllBooks(); // Kitap listesini güncelle
    } catch (error) {
        showAlert('Hata: ' + error.message, true);
    }
}

// İsteği reddet
async function rejectRequest(requestId) {
    if (!confirm('Bu ödünç isteğini reddetmek istediğinizden emin misiniz?')) {
        return;
    }
    
    try {
        await apiPost(`/admin/borrow-requests/${requestId}/reject`, {});
        showAlert('Ödünç isteği reddedildi.');
        getPendingRequests();
        getAllRequests();
        getAllBooks(); // Kitap listesini güncelle
    } catch (error) {
        showAlert('Hata: ' + error.message, true);
    }
}

// ============================================
// ÖDÜNÇ KAYITLARI
// ============================================

async function getAllBorrows() {
    try {
        const data = await apiGet('/admin/borrows');
        displayBorrowsTable(data);
    } catch (error) {
        showAlert('Ödünç kayıtları yüklenirken hata oluştu: ' + error.message, true);
    }
}

function displayBorrowsTable(borrows) {
    const container = document.getElementById('borrowsTable');
    if (!borrows || borrows.length === 0) {
        container.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Henüz ödünç kaydı yok.</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Kullanıcı</th><th>Kitap</th><th>Ödünç Tarihi</th><th>İade Tarihi</th><th>Durum</th></tr></thead><tbody>';
    
    borrows.forEach(borrow => {
        html += `
            <tr>
                <td>${borrow.id}</td>
                <td><strong>${borrow.user.username}</strong><br><small>${borrow.user.fullName}</small></td>
                <td><strong>${borrow.book.title}</strong><br><small>${borrow.book.author}</small></td>
                <td>${borrow.borrowDate}</td>
                <td>${borrow.returnDate || '-'}</td>
                <td><span class="badge ${borrow.status}">${borrow.status === 'ACTIVE' ? 'Aktif' : 'İade Edildi'}</span></td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

// ============================================
// KİTAP ARAMA FONKSİYONLARI
// ============================================

// Kitap ara
async function searchBooks() {
    const keyword = document.getElementById('searchInput').value.trim();
    
    if (!keyword) {
        showAlert('Lütfen arama terimi girin!', true);
        return;
    }
    
    try {
        // URL'deki özel karakterleri encode et
        const encodedKeyword = encodeURIComponent(keyword);
        // Admin için user endpoint'ini kullanabiliriz (her ikisi de aynı servisi kullanıyor)
        const data = await apiGet(`/user/books/title/${encodedKeyword}`);
        displaySearchResults(data, keyword);
    } catch (error) {
        showAlert('Arama sırasında hata oluştu: ' + error.message, true);
    }
}

// Arama sonuçlarını göster (booksTable'a yazıyor)
function displaySearchResults(books, keyword) {
    const container = document.getElementById('booksTable');
    
    if (!books || books.length === 0) {
        container.innerHTML = `<p style="padding: 20px; text-align: center; color: #666;">"${keyword}" için sonuç bulunamadı.</p>`;
        return;
    }
    
    let html = `<p style="padding: 10px; color: #666; font-weight: 600;">"${keyword}" için ${books.length} sonuç bulundu:</p>`;
    html += '<table><thead><tr><th>ID</th><th>Başlık</th><th>Yazar</th><th>ISBN</th><th>Yıl</th><th>Toplam</th><th>Mevcut</th><th>Durum</th></tr></thead><tbody>';
    
    books.forEach(book => {
        const available = book.availableCopies > 0;
        html += `
            <tr>
                <td>${book.id}</td>
                <td><strong>${book.title}</strong></td>
                <td>${book.author}</td>
                <td>${book.isbn}</td>
                <td>${book.publicationYear || '-'}</td>
                <td>${book.totalCopies}</td>
                <td>${book.availableCopies}</td>
                <td><span class="badge ${available ? 'available' : 'unavailable'}">${available ? 'Mevcut' : 'Tükendi'}</span></td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

// Sayfa yüklendiğinde kitapları ve bekleyen istekleri getir
window.onload = function() {
    getAllBooks();
    // Bekleyen istekleri de yükle (arka planda)
    setTimeout(getPendingRequests, 500);
    
    // Enter tuşu ile arama yapma
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchBooks();
            }
        });
    }
    
    // Tümünü Göster butonuna tıklanınca arama input'unu temizle
    const showAllBtn = document.querySelector('button[onclick="getAllBooks()"]');
    if (showAllBtn) {
        showAllBtn.addEventListener('click', function() {
            const searchInput = document.getElementById('searchInput');
            if (searchInput) {
                searchInput.value = '';
            }
        });
    }
};

