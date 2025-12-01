/* resources/static/js/admin.js (GÜNCELLENMİŞ VERSİYON) */

document.addEventListener("DOMContentLoaded", function() {
    loadStats();
    loadBooks();
    document.getElementById('view-books').style.display = 'block';
});

// GÖRÜNÜM DEĞİŞTİRME
function showView(viewName) {
    document.querySelectorAll('.nav-menu a').forEach(el => el.classList.remove('active'));
    event.target.closest('a').classList.add('active');

    document.querySelectorAll('.view-section').forEach(el => el.style.display = 'none');
    const targetView = document.getElementById(`view-${viewName}`);
    if(targetView) targetView.style.display = 'block';

    if(viewName === 'books') loadBooks();
    if(viewName === 'requests') loadRequests();
    if(viewName === 'users') loadUsers();
}

// 1. İSTATİSTİKLERİ YÜKLE
async function loadStats() {
    try {
        const [books, users, requests, borrows] = await Promise.all([
            apiGet('/admin/books'),
            apiGet('/admin/users'),
            apiGet('/admin/borrow-requests/pending'),
            apiGet('/admin/borrows')
        ]);

        const pendingCount = requests.filter(r => r.status === 'PENDING').length;
        const activeBorrowsCount = borrows.filter(b => b.status === 'ACTIVE').length;

        document.getElementById('countBooks').innerText = books.length;
        document.getElementById('countUsers').innerText = users.length;
        document.getElementById('countRequests').innerText = pendingCount;
        document.getElementById('countBorrows').innerText = activeBorrowsCount;

        const badge = document.getElementById('badgeRequests');
        if(pendingCount > 0) {
            badge.style.display = 'inline-block';
            badge.innerText = pendingCount;
        } else {
            badge.style.display = 'none';
        }
    } catch (e) { console.error(e); }
}

// 2. KİTAPLARI LİSTELE (GÜNCELLENDİ: Tüm verileri taşıyor)
async function loadBooks() {
    const container = document.getElementById('booksListContainer');
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-success"></div></div>';

    try {
        const books = await apiGet('/admin/books');
        if(books.length === 0) {
            container.innerHTML = '<div class="text-muted text-center">Kitap yok.</div>';
            return;
        }

        let html = '';
        books.forEach(book => {
            // Tırnak işaretleri hatası olmasın diye escape yapıyoruz
            const safeTitle = book.title.replace(/'/g, "\\'");
            const safeAuthor = book.author.replace(/'/g, "\\'");
            const year = book.publicationYear || 0;

            html += `
            <div class="custom-list-item">
                <div class="item-left">
                    <div class="item-icon" style="background:#e0f2f1; color:#009688;">
                        <i class="fas fa-book"></i>
                    </div>
                    <div>
                        <h6 class="m-0 fw-bold">${book.title}</h6>
                        <span class="text-muted" style="font-size:12px;">${book.author} • Stok: ${book.availableCopies}/${book.totalCopies}</span>
                    </div>
                </div>
                <div class="action-buttons">
                    <button onclick="openDeleteModal(${book.id}, '${safeTitle}', '${safeAuthor}', '${book.isbn}', ${year}, ${book.totalCopies}, ${book.availableCopies})"
                            class="btn btn-sm btn-light text-danger" title="Sil / Stok Düş">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>`;
        });
        container.innerHTML = html;
    } catch (e) { container.innerHTML = 'Hata oluştu.'; }
}

/* ==================================================
   SİLME VE STOK İŞLEMLERİ (HATA DÜZELTİLDİ)
   ================================================== */

// Modalı Açan Fonksiyon (Verileri alıp gizli inputlara yazar)
function openDeleteModal(id, title, author, isbn, year, total, available) {
    document.getElementById('modalDeleteId').value = id;
    document.getElementById('modalDeleteTitle').innerText = title;

    // Gizli alanları doldur
    document.getElementById('modalDeleteTitleHidden').value = title;
    document.getElementById('modalDeleteAuthor').value = author;
    document.getElementById('modalDeleteIsbn').value = isbn;
    document.getElementById('modalDeleteYear').value = year;

    document.getElementById('modalCurrentTotal').innerText = total;
    document.getElementById('modalCurrentAvailable').innerText = available;
    document.getElementById('deleteAmount').value = '';

    new bootstrap.Modal(document.getElementById('deleteModal')).show();
}

// A. Stok Düşür (DÜZELTİLDİ: Tüm nesneyi gönderiyor)
async function confirmReduceStock() {
    const id = document.getElementById('modalDeleteId').value;
    const amount = parseInt(document.getElementById('deleteAmount').value);
    const currentTotal = parseInt(document.getElementById('modalCurrentTotal').innerText);
    const currentAvailable = parseInt(document.getElementById('modalCurrentAvailable').innerText);

    // Diğer verileri de al (Validation hatasını önlemek için)
    const title = document.getElementById('modalDeleteTitleHidden').value;
    const author = document.getElementById('modalDeleteAuthor').value;
    const isbn = document.getElementById('modalDeleteIsbn').value;
    const year = parseInt(document.getElementById('modalDeleteYear').value);

    if (!amount || amount <= 0) {
        showToast('Uyarı', 'Lütfen geçerli bir sayı girin.');
        return;
    }

    if (amount > currentAvailable) {
        showToast('Hata', 'Raftaki stoktan daha fazlasını silemezsiniz!');
        return;
    }

    // Backend validation kurallarına uymak için tüm objeyi oluşturuyoruz
    const updateData = {
        title: title,
        author: author,
        isbn: isbn,
        publicationYear: year === 0 ? null : year,
        totalCopies: currentTotal - amount,
        availableCopies: currentAvailable - amount
    };

    try {
        await apiPut(`/admin/books/${id}`, updateData);
        showToast('Başarılı', `${amount} adet kitap stoktan silindi.`);

        const modalEl = document.getElementById('deleteModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal.hide();

        loadBooks();
        loadStats();
    } catch (e) {
        showToast('Hata', e.message);
    }
}

// B. Kitabı Tamamen Sil
async function confirmDeleteCompletely() {
    const id = document.getElementById('modalDeleteId').value;
    if(!confirm('DİKKAT! Bu kitabı tamamen silmek üzeresiniz. Emin misiniz?')) return;

    try {
        await apiDelete(`/admin/books/${id}`);
        showToast('Bilgi', 'Kitap tamamen silindi.');

        const modalEl = document.getElementById('deleteModal');
        const modal = bootstrap.Modal.getInstance(modalEl);
        modal.hide();

        loadBooks();
        loadStats();
    } catch(e) {
        showToast('Hata', e.message);
    }
}

// 3. KİTAP EKLE
function toggleAddBookForm() {
    const form = document.getElementById('addBookFormContainer');
    form.style.display = form.style.display === 'none' ? 'block' : 'none';
}

async function addBook() {
    const book = {
        title: document.getElementById('bookTitle').value,
        author: document.getElementById('bookAuthor').value,
        isbn: document.getElementById('bookIsbn').value,
        publicationYear: parseInt(document.getElementById('bookYear').value) || 2024,
        totalCopies: parseInt(document.getElementById('bookTotal').value) || 5,
        availableCopies: parseInt(document.getElementById('bookTotal').value) || 5
    };

    try {
        await apiPost('/admin/books', book);
        showToast('Başarılı', 'Kitap eklendi!');
        document.getElementById('bookTitle').value = '';
        document.getElementById('bookIsbn').value = '';
        loadBooks();
        loadStats();
    } catch(e) { showToast('Hata', e.message); }
}

// 5. İSTEKLER VE DİĞER FONKSİYONLAR AYNEN KALIYOR...
// (Eski admin.js'deki loadRequests, approveRequest, rejectRequest, loadUsers, showToast fonksiyonları burada aynen kalmalı)
// Yer kaplamasın diye tekrar yazmadım, alt kısımlar aynı.

async function loadRequests() {
    const container = document.getElementById('requestsListContainer');
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-danger"></div></div>';
    try {
        const requests = await apiGet('/admin/borrow-requests/pending');
        const pending = requests.filter(r => r.status === 'PENDING');
        if(pending.length === 0) {
            container.innerHTML = '<div class="text-muted text-center">Bekleyen istek yok.</div>';
            return;
        }
        let html = '';
        pending.forEach(req => {
            html += `
            <div class="custom-list-item">
                <div class="item-left">
                    <div class="item-icon" style="background:#ffebee; color:#e53935;">
                        <i class="fas fa-user-clock"></i>
                    </div>
                    <div>
                        <h6 class="m-0 fw-bold">${req.user.fullName} (@${req.user.username})</h6>
                        <span class="text-muted" style="font-size:12px;">İstediği: <strong>${req.book.title}</strong> • Tarih: ${req.requestDate}</span>
                    </div>
                </div>
                <div class="action-buttons">
                    <button onclick="approveRequest(${req.id})" class="btn btn-success btn-sm"><i class="fas fa-check"></i> Onayla</button>
                    <button onclick="rejectRequest(${req.id})" class="btn btn-outline-danger btn-sm"><i class="fas fa-times"></i> Reddet</button>
                </div>
            </div>`;
        });
        container.innerHTML = html;
    } catch(e) { container.innerHTML = 'Hata oluştu.'; }
}

async function approveRequest(id) {
    try {
        await apiPost(`/admin/borrow-requests/${id}/approve`, {});
        showToast('Başarılı', 'İstek onaylandı.');
        loadRequests();
        loadStats();
    } catch(e) { showToast('Hata', e.message); }
}

async function rejectRequest(id) {
    if(!confirm('Reddetmek istediğinize emin misiniz?')) return;
    try {
        await apiPost(`/admin/borrow-requests/${id}/reject`, {});
        showToast('Bilgi', 'İstek reddedildi.');
        loadRequests();
        loadStats();
    } catch(e) { showToast('Hata', e.message); }
}

async function loadUsers() {
    const container = document.getElementById('usersListContainer');
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-primary"></div></div>';
    try {
        const users = await apiGet('/admin/users');
        let html = '';
        users.forEach(u => {
            html += `
            <div class="custom-list-item">
                <div class="item-left">
                    <div class="item-icon" style="background:#e3f2fd; color:#1565c0;">
                        <i class="fas fa-user"></i>
                    </div>
                    <div>
                        <h6 class="m-0 fw-bold">${u.fullName}</h6>
                        <span class="text-muted" style="font-size:12px;">@${u.username} • ${u.email}</span>
                    </div>
                </div>
                <div>
                    <span class="badge ${u.role === 'ADMIN' ? 'bg-danger' : 'bg-primary'}">${u.role}</span>
                </div>
            </div>`;
        });
        container.innerHTML = html;
    } catch(e) { container.innerHTML = 'Hata.'; }
}

function showToast(title, msg) {
    document.getElementById('toastTitle').innerText = title;
    document.getElementById('toastMessage').innerText = msg;
    new bootstrap.Toast(document.getElementById('adminToast')).show();
}