/* resources/static/js/admin.js */

document.addEventListener("DOMContentLoaded", function() {
    loadStats();
    // VARSAYILAN OLARAK KİTAPLAR SEKMESİ AÇILSIN
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

        // FİLTRELİ SAYIM (Hata düzeltilmiş hali)
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

// 2. KİTAPLARI LİSTELE
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
                    <button onclick="deleteBook(${book.id})" class="btn btn-sm btn-light text-danger" title="Sil">
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>`;
        });
        container.innerHTML = html;
    } catch (e) { container.innerHTML = 'Hata oluştu.'; }
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

// 4. KİTAP SİL
async function deleteBook(id) {
    if(!confirm('Bu kitabı silmek istediğinize emin misiniz?')) return;
    try {
        await apiDelete(`/admin/books/${id}`);
        showToast('Bilgi', 'Kitap silindi.');
        loadBooks();
        loadStats();
    } catch(e) { showToast('Hata', e.message); }
}

// 5. İSTEKLERİ LİSTELE
async function loadRequests() {
    const container = document.getElementById('requestsListContainer');
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-danger"></div></div>';

    try {
        const requests = await apiGet('/admin/borrow-requests/pending');
        // Sadece bekleyenleri filtrele
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

// 6. KULLANICILARI LİSTELE
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