/* resources/static/js/admin.js */

document.addEventListener("DOMContentLoaded", function() {
    loadStats();
    loadBooks();
    document.getElementById('view-books').style.display = 'block';

    // ARAMA DİNLEYİCİSİ (YENİLENDİ)
    const searchInput = document.getElementById('adminSearchInput');
    if(searchInput) {
        searchInput.addEventListener('keyup', function(e) {
            searchAdminBooks(e.target.value);
        });
    }
});

function showView(viewName) {
    document.querySelectorAll('.nav-menu a').forEach(el => el.classList.remove('active'));
    event.target.closest('a').classList.add('active');

    document.querySelectorAll('.view-section').forEach(el => el.style.display = 'none');
    const targetView = document.getElementById(`view-${viewName}`);
    if(targetView) targetView.style.display = 'block';

    if(viewName === 'books') loadBooks();
    if(viewName === 'requests') loadRequests();
    if(viewName === 'users') loadUsers();
    if(viewName === 'profile-requests') loadProfileRequests(); // YENİ
}

// ... (loadStats, loadBooks, loadRequests, vb. ESKİ FONKSİYONLAR AYNI KALACAK) ...
// Buradaki tek fark "loadUsers" içine "Sil" butonu eklemek ve aşağıya yeni fonksiyonları eklemek.

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
        if(pendingCount > 0) { badge.style.display = 'inline-block'; badge.innerText = pendingCount; }
        else { badge.style.display = 'none'; }
    } catch (e) { console.error(e); }
}

async function loadBooks() {
    const container = document.getElementById('booksListContainer');
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-success"></div></div>';
    try {
        const books = await apiGet('/admin/books');
        if(books.length === 0) { container.innerHTML = '<div class="text-muted text-center">Kitap yok.</div>'; return; }
        let html = '';
        books.forEach(book => {
            const safeTitle = book.title.replace(/'/g, "\\'");
            const safeAuthor = book.author.replace(/'/g, "\\'");
            const year = book.publicationYear || 0;
            html += `
            <div class="custom-list-item">
                <div class="item-left">
                    <div class="item-icon" style="background:#e0f2f1; color:#009688;"><i class="fas fa-book"></i></div>
                    <div><h6 class="m-0 fw-bold">${book.title}</h6><span class="text-muted" style="font-size:12px;">${book.author} • Stok: ${book.availableCopies}/${book.totalCopies}</span></div>
                </div>
                <div class="action-buttons">
                    <button onclick="openEditModal(${book.id}, '${safeTitle}', '${safeAuthor}', '${book.isbn}', ${year}, ${book.totalCopies}, ${book.availableCopies})" class="btn btn-sm btn-light text-primary"><i class="fas fa-edit"></i></button>
                    <button onclick="openDeleteModal(${book.id}, '${safeTitle}', '${safeAuthor}', '${book.isbn}', ${year}, ${book.totalCopies}, ${book.availableCopies})" class="btn btn-sm btn-light text-danger"><i class="fas fa-trash"></i></button>
                </div>
            </div>`;
        });
        container.innerHTML = html;
    } catch (e) { container.innerHTML = 'Hata oluştu.'; }
}

async function loadUsers() {
    const container = document.getElementById('usersListContainer');
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-primary"></div></div>';
    try {
        const users = await apiGet('/admin/users');
        let html = '';
        users.forEach(u => {
            // YENİ: SİL BUTONU
            const deleteBtn = u.role === 'ADMIN' ? '' : `<button onclick="deleteUser(${u.id})" class="btn btn-sm btn-light text-danger"><i class="fas fa-trash-alt"></i></button>`;

            html += `
            <div class="custom-list-item">
                <div class="item-left">
                    <div class="item-icon" style="background:#e3f2fd; color:#1565c0;"><i class="fas fa-user"></i></div>
                    <div><h6 class="m-0 fw-bold">${u.fullName}</h6><span class="text-muted" style="font-size:12px;">@${u.username} • ${u.email}</span></div>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <span class="badge ${u.role === 'ADMIN' ? 'bg-danger' : 'bg-primary'}">${u.role}</span>
                    ${deleteBtn}
                </div>
            </div>`;
        });
        container.innerHTML = html;
    } catch(e) { container.innerHTML = 'Hata.'; }
}

async function searchAdminBooks(query) {
    const container = document.getElementById('booksListContainer');

    // Eğer kutu boşsa normal listeyi getir
    if (!query || query.trim() === '') {
        loadBooks();
        return;
    }

    try {
        const books = await apiGet(`/admin/books/search?title=${encodeURIComponent(query)}`);

        if(books.length === 0) {
            container.innerHTML = '<div class="text-muted text-center py-3">Sonuç bulunamadı.</div>';
            return;
        }

        // Listeyi Çiz (loadBooks içindeki yapının aynısı)
        let html = '';
        books.forEach(book => {
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
                    <button onclick="openEditModal(${book.id}, '${safeTitle}', '${safeAuthor}', '${book.isbn}', ${year}, ${book.totalCopies}, ${book.availableCopies})"
                            class="btn btn-sm btn-light text-primary"><i class="fas fa-edit"></i></button>
                    <button onclick="openDeleteModal(${book.id}, '${safeTitle}', '${safeAuthor}', '${book.isbn}', ${year}, ${book.totalCopies}, ${book.availableCopies})"
                            class="btn btn-sm btn-light text-danger"><i class="fas fa-trash"></i></button>
                </div>
            </div>`;
        });
        container.innerHTML = html;

    } catch (e) { console.error(e); }
}

// === YENİ: KULLANICI SİLME ===
async function deleteUser(id) {
    if(!confirm("DİKKAT! Bu kullanıcıyı sildiğinizde tüm geçmişi silinir. Emin misiniz?")) return;
    try {
        await apiDelete(`/admin/users/${id}`);
        showToast('Başarılı', 'Kullanıcı silindi.');
        loadUsers(); loadStats();
    } catch(e) { showToast('Hata', e.message); }
}

// === YENİ: PROFİL İSTEKLERİNİ YÖNETME ===
async function loadProfileRequests() {
    const container = document.getElementById('profileRequestsContainer');
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-primary"></div></div>';
    try {
        const reqs = await apiGet('/admin/profile-requests/pending');
        if(reqs.length === 0) { container.innerHTML = '<div class="text-muted text-center">Bekleyen profil isteği yok.</div>'; return; }
        let html = '';
        reqs.forEach(r => {
            html += `
            <div class="custom-list-item">
                <div class="item-left">
                    <div class="item-icon" style="background:#fff3e0; color:#ef6c00;"><i class="fas fa-user-edit"></i></div>
                    <div>
                        <div class="mb-1"><span class="fw-bold">${r.user.username}</span> <i class="fas fa-arrow-right mx-2 text-muted" style="font-size:12px;"></i> <span class="text-primary fw-bold">${r.newUsername}</span></div>
                        <div class="small text-muted">${r.user.email} <i class="fas fa-arrow-right mx-2" style="font-size:10px;"></i> ${r.newEmail}</div>
                    </div>
                </div>
                <div class="action-buttons">
                    <button onclick="approveProfile(${r.id})" class="btn btn-success btn-sm">Onayla</button>
                    <button onclick="rejectProfile(${r.id})" class="btn btn-outline-danger btn-sm">Reddet</button>
                </div>
            </div>`;
        });
        container.innerHTML = html;
    } catch(e) { container.innerHTML = 'Hata oluştu.'; }
}

async function approveProfile(id) {
    try { await apiPost(`/admin/profile-requests/${id}/approve`, {}); showToast('Başarılı', 'Profil güncellendi.'); loadProfileRequests(); }
    catch(e) { showToast('Hata', e.message); }
}
async function rejectProfile(id) {
    if(!confirm("Reddet?")) return;
    try { await apiPost(`/admin/profile-requests/${id}/reject`, {}); showToast('Bilgi', 'Reddedildi.'); loadProfileRequests(); }
    catch(e) { showToast('Hata', e.message); }
}

// === DİĞER (Kitap Ekle/Sil/Düzenle, Ödünç İstekleri) KODLAR AYNEN KALACAK ===
// (Yer kaplamaması için buraya tekrar yapıştırmıyorum, önceki admin.js'den kopyalayabilirsin.
// Sadece yukarıdaki loadBooks, loadUsers ve yeni fonksiyonları güncellemen yeterli)

function openDeleteModal(id, title, author, isbn, year, total, available) {
    document.getElementById('modalDeleteId').value = id;
    document.getElementById('modalDeleteTitle').innerText = title;
    document.getElementById('modalDeleteTitleHidden').value = title;
    document.getElementById('modalDeleteAuthor').value = author;
    document.getElementById('modalDeleteIsbn').value = isbn;
    document.getElementById('modalDeleteYear').value = year;
    document.getElementById('modalCurrentTotal').innerText = total;
    document.getElementById('modalCurrentAvailable').innerText = available;
    document.getElementById('deleteAmount').value = '';
    new bootstrap.Modal(document.getElementById('deleteModal')).show();
}

async function confirmReduceStock() {
    const id = document.getElementById('modalDeleteId').value;
    const amount = parseInt(document.getElementById('deleteAmount').value);
    const currentTotal = parseInt(document.getElementById('modalCurrentTotal').innerText);
    const currentAvailable = parseInt(document.getElementById('modalCurrentAvailable').innerText);
    const title = document.getElementById('modalDeleteTitleHidden').value;
    const author = document.getElementById('modalDeleteAuthor').value;
    const isbn = document.getElementById('modalDeleteIsbn').value;
    const year = parseInt(document.getElementById('modalDeleteYear').value);

    if (!amount || amount <= 0) { showToast('Uyarı', 'Geçersiz sayı.'); return; }
    if (amount > currentAvailable) { showToast('Hata', 'Raftakinden fazla silemezsiniz.'); return; }

    const updateData = {
        title: title, author: author, isbn: isbn, publicationYear: year === 0 ? null : year,
        totalCopies: currentTotal - amount,
        availableCopies: currentAvailable - amount
    };

    try {
        await apiPut(`/admin/books/${id}`, updateData);
        showToast('Başarılı', `Stok düşüldü.`);
        bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
        loadBooks(); loadStats();
    } catch (e) { showToast('Hata', e.message); }
}

async function confirmDeleteCompletely() {
    const id = document.getElementById('modalDeleteId').value;
    if(!confirm('Kalıcı olarak silinecek. Emin misiniz?')) return;
    try {
        await apiDelete(`/admin/books/${id}`);
        showToast('Bilgi', 'Kitap silindi.');
        bootstrap.Modal.getInstance(document.getElementById('deleteModal')).hide();
        loadBooks(); loadStats();
    } catch(e) { showToast('Hata', e.message); }
}

function openEditModal(id, title, author, isbn, year, total, available) {
    document.getElementById('editBookId').value = id;
    document.getElementById('editTitle').value = title;
    document.getElementById('editAuthor').value = author;
    document.getElementById('editIsbn').value = isbn;
    document.getElementById('editYear').value = year;
    document.getElementById('editTotal').value = total;
    document.getElementById('editOldTotal').value = total;
    document.getElementById('editOldAvailable').value = available;
    new bootstrap.Modal(document.getElementById('editBookModal')).show();
}

async function saveBookUpdate() {
    const id = document.getElementById('editBookId').value;
    const newTotal = parseInt(document.getElementById('editTotal').value);
    const oldTotal = parseInt(document.getElementById('editOldTotal').value);
    const oldAvailable = parseInt(document.getElementById('editOldAvailable').value);
    const difference = newTotal - oldTotal;
    const newAvailable = oldAvailable + difference;

    if (newAvailable < 0) { showToast('Hata', 'Stok hatası.'); return; }

    const updatedBook = {
        title: document.getElementById('editTitle').value,
        author: document.getElementById('editAuthor').value,
        isbn: document.getElementById('editIsbn').value,
        publicationYear: parseInt(document.getElementById('editYear').value),
        totalCopies: newTotal,
        availableCopies: newAvailable
    };

    try {
        await apiPut(`/admin/books/${id}`, updatedBook);
        showToast('Başarılı', 'Güncellendi.');
        bootstrap.Modal.getInstance(document.getElementById('editBookModal')).hide();
        loadBooks(); loadStats();
    } catch(e) { showToast('Hata', e.message); }
}

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
        loadBooks(); loadStats();
    } catch(e) { showToast('Hata', e.message); }
}

async function loadRequests() {
    const container = document.getElementById('requestsListContainer');
    container.innerHTML = '<div class="text-center py-3"><div class="spinner-border text-danger"></div></div>';
    try {
        const requests = await apiGet('/admin/borrow-requests/pending');
        const pending = requests.filter(r => r.status === 'PENDING');
        if(pending.length === 0) { container.innerHTML = '<div class="text-muted text-center">Bekleyen istek yok.</div>'; return; }
        let html = '';
        pending.forEach(req => {
            html += `
            <div class="custom-list-item">
                <div class="item-left">
                    <div class="item-icon" style="background:#ffebee; color:#e53935;"><i class="fas fa-user-clock"></i></div>
                    <div><h6 class="m-0 fw-bold">${req.user.fullName}</h6><span class="text-muted" style="font-size:12px;">İstediği: <strong>${req.book.title}</strong></span></div>
                </div>
                <div class="action-buttons">
                    <button onclick="approveRequest(${req.id})" class="btn btn-success btn-sm">Onayla</button>
                    <button onclick="rejectRequest(${req.id})" class="btn btn-outline-danger btn-sm">Reddet</button>
                </div>
            </div>`;
        });
        container.innerHTML = html;
    } catch(e) { container.innerHTML = 'Hata oluştu.'; }
}

async function approveRequest(id) {
    try { await apiPost(`/admin/borrow-requests/${id}/approve`, {}); showToast('Başarılı', 'Onaylandı.'); loadRequests(); loadStats(); }
    catch(e) { showToast('Hata', e.message); }
}
async function rejectRequest(id) {
    if(!confirm('Reddet?')) return;
    try { await apiPost(`/admin/borrow-requests/${id}/reject`, {}); showToast('Bilgi', 'Reddedildi.'); loadRequests(); loadStats(); }
    catch(e) { showToast('Hata', e.message); }
}

function showToast(title, msg) {
    document.getElementById('toastTitle').innerText = title;
    document.getElementById('toastMessage').innerText = msg;
    new bootstrap.Toast(document.getElementById('adminToast')).show();
}