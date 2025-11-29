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

// Sayfa yüklendiğinde kitapları getir
window.onload = function() {
    getAllBooks();
};

