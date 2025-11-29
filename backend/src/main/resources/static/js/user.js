/* ============================================
   USER SAYFASI JAVASCRIPT
   ============================================ */

let currentUsername = '';

// Alert göster (Toast notification kullanıyor)
function showAlert(message, isError = false) {
    const type = isError ? 'error' : 'success';
    showToast(message, type);
}

// Kullanıcı verilerini yükle
async function loadUserData() {
    const username = document.getElementById('selectedUsername').value.trim();
    if (!username) {
        showAlert('Lütfen kullanıcı adı girin!', true);
        return;
    }
    
    try {
        const data = await apiGet(`/user/borrows/username/${username}`);
        currentUsername = username;
        document.getElementById('currentUsername').textContent = username;
        document.getElementById('currentUserInfo').style.display = 'block';
        showAlert('Kullanıcı bilgileri yüklendi!');
        getUserBorrows();
    } catch (error) {
        showAlert('Kullanıcı bulunamadı: ' + error.message, true);
    }
}

// Kitap ödünç al
async function borrowBook() {
    if (!currentUsername) {
        showAlert('Lütfen önce kullanıcı adını seçin ve "Yükle" butonuna tıklayın!', true);
        return;
    }
    
    const bookId = document.getElementById('borrowBookId').value;
    if (!bookId) {
        showAlert('Lütfen kitap ID girin!', true);
        return;
    }
    
    try {
        const data = await apiPost(`/user/borrow/username/${currentUsername}/${bookId}`, {});
        showAlert('Kitap başarıyla ödünç alındı!');
        document.getElementById('borrowBookId').value = '';
        getUserBorrows();
    } catch (error) {
        showAlert('Hata: ' + error.message, true);
    }
}

// Kitap iade et
async function returnBook() {
    if (!currentUsername) {
        showAlert('Lütfen önce kullanıcı adını seçin ve "Yükle" butonuna tıklayın!', true);
        return;
    }
    
    const borrowId = document.getElementById('returnBookId').value;
    if (!borrowId) {
        showAlert('Lütfen ödünç ID girin!', true);
        return;
    }
    
    try {
        const data = await apiPost(`/user/return/username/${currentUsername}/${borrowId}`, {});
        showAlert('Kitap başarıyla iade edildi!');
        document.getElementById('returnBookId').value = '';
        getUserBorrows();
    } catch (error) {
        showAlert('Hata: ' + error.message, true);
    }
}

// Kullanıcının ödünç kayıtlarını getir
async function getUserBorrows() {
    if (!currentUsername) {
        document.getElementById('borrowsTable').innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Lütfen önce kullanıcı adını seçin.</p>';
        return;
    }
    
    try {
        const data = await apiGet(`/user/borrows/username/${currentUsername}`);
        displayBorrowsTable(data);
    } catch (error) {
        showAlert('Ödünç kayıtları yüklenirken hata oluştu: ' + error.message, true);
    }
}

// Ödünç kayıtlarını göster
function displayBorrowsTable(borrows) {
    const container = document.getElementById('borrowsTable');
    if (!borrows || borrows.length === 0) {
        container.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Henüz ödünç kaydınız yok.</p>';
        return;
    }
    
    let html = '<table><thead><tr><th>ID</th><th>Kitap</th><th>Yazar</th><th>Ödünç Tarihi</th><th>İade Tarihi</th><th>Durum</th></tr></thead><tbody>';
    
    borrows.forEach(borrow => {
        html += `
            <tr>
                <td>${borrow.id}</td>
                <td><strong>${borrow.book.title}</strong></td>
                <td>${borrow.book.author}</td>
                <td>${borrow.borrowDate}</td>
                <td>${borrow.returnDate || '-'}</td>
                <td><span class="badge ${borrow.status}">${borrow.status === 'ACTIVE' ? 'Aktif' : 'İade Edildi'}</span></td>
            </tr>
        `;
    });
    
    html += '</tbody></table>';
    container.innerHTML = html;
}

// Tüm kitapları göster (arama sonuçları bölümünde)
async function showAllBooks() {
    try {
        const data = await apiGet('/user/books/all');
        displayAllBooksInSearchResults(data);
        // Arama input'unu sıfırla
        document.getElementById('searchInput').value = '';
    } catch (error) {
        showAlert('Kitaplar yüklenirken hata oluştu: ' + error.message, true);
    }
}

// Tüm kitapları arama sonuçları bölümünde göster
function displayAllBooksInSearchResults(books) {
    const container = document.getElementById('searchResults');
    if (!books || books.length === 0) {
        container.innerHTML = '<p style="padding: 20px; text-align: center; color: #666;">Henüz kitap eklenmemiş.</p>';
        return;
    }
    
    let html = `<p style="padding: 10px; color: #666; font-weight: 600;">Tüm kitaplar (${books.length} adet):</p>`;
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
        const data = await apiGet(`/user/books/title/${encodedKeyword}`);
        displaySearchResults(data, keyword);
    } catch (error) {
        showAlert('Arama sırasında hata oluştu: ' + error.message, true);
    }
}

// Arama sonuçlarını göster
function displaySearchResults(books, keyword) {
    const container = document.getElementById('searchResults');
    
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

// Sayfa yüklendiğinde hiçbir şey yapma (kullanıcı arama yapana kadar bekle)

