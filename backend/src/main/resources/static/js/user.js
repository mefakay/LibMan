/* resources/static/js/user.js */

const username = document.getElementById('currentUsername').value;
let allBooks = [];

document.addEventListener("DOMContentLoaded", function() {
    loadBooks();
    updateStats();

    // ARAMA DİNLEYİCİSİ
    const searchInput = document.getElementById('searchInput');
    if(searchInput) {
        searchInput.addEventListener('keyup', function(e) {
            searchUserBooks(e.target.value);
        });
    }
});

/* MENÜ GEÇİŞ */
function showSection(section, element) {
    document.querySelectorAll('.nav-menu a').forEach(el => el.classList.remove('active'));
    if(element) element.classList.add('active');

    const listTitle = document.getElementById('listTitle');
    const container = document.getElementById('bookListContainer');
    const searchContainer = document.getElementById('searchContainer'); // Arama kutusunun div'i
    const refreshBtn = document.getElementById('refreshBtn');

    container.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-success"></div></div>';

    if (section === 'catalog') {
        listTitle.innerText = '📚 Kitap Kataloğu';
        searchContainer.style.display = 'flex'; // Katalogda GÖSTER
        refreshBtn.setAttribute('onclick', 'loadBooks()');
        loadBooks();
    } else if (section === 'mybooks') {
        listTitle.innerText = '📖 Ödünç Aldıklarım';
        searchContainer.style.display = 'none'; // Diğerlerinde GİZLE
        refreshBtn.setAttribute('onclick', 'loadMyBooks()');
        loadMyBooks();
    } else if (section === 'requests') {
        listTitle.innerText = '⏳ İsteklerim';
        searchContainer.style.display = 'none'; // Diğerlerinde GİZLE
        refreshBtn.setAttribute('onclick', 'loadMyRequests()');
        loadMyRequests();
    }
}

// loadBooks, loadMyBooks, loadMyRequests, updateStats, searchUserBooks

async function searchUserBooks(query) {
    const container = document.getElementById('bookListContainer');

    // Sadece Katalog sekmesinde çalışsın
    const activeLink = document.querySelector('.nav-menu a.active');
    if(activeLink && !activeLink.innerText.includes('Ana Panel')) {
         return;
    }

    try {
        const books = await apiGet(`/user/books/search?title=${encodeURIComponent(query)}`);
        if (!books || books.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-3">Sonuç bulunamadı.</div>';
            return;
        }
        renderList(books);
    } catch (error) { console.error(error); }
}

async function loadBooks() {
    const container = document.getElementById('bookListContainer');
    try {
        const books = await apiGet('/user/books/all');
        allBooks = books;
        if (!books || books.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-3">Kütüphanede kitap bulunamadı.</div>';
            return;
        }
        renderList(books);
    } catch (error) { showError(container, error); }
}

async function loadMyBooks() {
    const container = document.getElementById('bookListContainer');
    try {
        const borrows = await apiGet(`/user/borrows/username/${username}`);
        if (!borrows || borrows.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-3">Henüz ödünç aldığınız kitap yok.</div>';
            return;
        }
        borrows.sort((a, b) => (a.status === 'ACTIVE' ? -1 : 1));
        let html = '';
        borrows.forEach(b => {
            const isActive = b.status === 'ACTIVE';
            const title = b.book ? b.book.title : 'Bilinmeyen Kitap';
            const author = b.book ? b.book.author : '-';
            html += `
            <div class="custom-list-item">
                <div class="item-left">
                    <div class="item-icon" style="background:#e3f2fd; color:#1565c0;">
                        <i class="fas fa-bookmark"></i>
                    </div>
                    <div class="item-info">
                        <h5>${title}</h5>
                        <span>${author} • ${b.borrowDate}</span>
                    </div>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <span class="status-badge" style="${isActive ? 'background:#e3f2fd; color:#1565c0' : 'background:#eee; color:#666'}">
                        ${isActive ? 'Okunuyor' : 'İade Edildi'}
                    </span>
                    ${isActive ? `<button onclick="returnBookViaList(${b.id})" class="btn btn-sm btn-outline-danger">İade Et</button>` : ''}
                </div>
            </div>`;
        });
        container.innerHTML = html;
    } catch (error) { showError(container, error); }
}

async function loadMyRequests() {
    const container = document.getElementById('bookListContainer');
    try {
        const requests = await apiGet(`/user/${username}/borrow-requests`);
        if (!requests || requests.length === 0) {
            container.innerHTML = '<div class="text-center text-muted py-3">Ödünç isteğiniz yok.</div>';
            return;
        }
        requests.sort((a, b) => b.id - a.id);
        let html = '';
        requests.forEach(r => {
            let style = r.status === 'PENDING' ? 'background:#fff8e1; color:#ffa000' :
                        (r.status === 'APPROVED' ? 'background:#e6f7f4; color:#00b894' : 'background:#ffebee; color:#c62828');
            let text = r.status === 'PENDING' ? '⏳ Beklemede' : (r.status === 'APPROVED' ? '✅ Onaylandı' : '❌ Reddedildi');
            const title = r.book ? r.book.title : 'Kitap Silinmiş';
            html += `
            <div class="custom-list-item">
                <div class="item-left">
                    <div class="item-icon" style="background:#f3e5f5; color:#8e24aa;">
                        <i class="fas fa-clock"></i>
                    </div>
                    <div class="item-info">
                        <h5>${title}</h5>
                        <span>İstek Tarihi: ${r.requestDate}</span>
                    </div>
                </div>
                <div><span class="status-badge" style="${style}">${text}</span></div>
            </div>`;
        });
        container.innerHTML = html;
    } catch (error) { showError(container, error); }
}

function renderList(list) {
    const container = document.getElementById('bookListContainer');
    let html = '';
    list.forEach(book => {
        const isAvailable = book.availableCopies > 0;
        html += `
        <div class="custom-list-item">
            <div class="item-left">
                <div class="item-icon" style="background:#e0f2f1; color:#009688;">
                    <i class="fas fa-book"></i>
                </div>
                <div class="item-info">
                    <h5>${book.title}</h5>
                    <span>${book.author} • ${book.publicationYear || '-'}</span>
                </div>
            </div>
            <div class="d-flex align-items-center gap-3">
                <span class="status-badge" style="${isAvailable ? 'background:#e6f7f4; color:#00b894' : 'background:#fff8e1; color:#ffa000'}">
                    ${isAvailable ? 'Mevcut' : 'Tükendi'}
                </span>
                ${isAvailable ? `<button onclick="borrowRequest(${book.id})" class="btn btn-sm btn-outline-success">İste</button>` :
                  `<button disabled class="btn btn-sm btn-light text-muted">Yok</button>`}
            </div>
        </div>`;
    });
    container.innerHTML = html;
}

// updateStats, showError, borrowRequest,

async function updateStats() {
    try {
        const [books, borrows, requests] = await Promise.all([
            apiGet('/user/books/all'),
            apiGet(`/user/borrows/username/${username}`),
            apiGet(`/user/${username}/borrow-requests`)
        ]);
        const pendingRequestsCount = requests.filter(r => r.status === 'PENDING').length;

        document.getElementById('totalBooksCount').innerText = books.length;
        document.getElementById('pendingRequestsCount').innerText = pendingRequestsCount;
        updateRecentActivity(borrows, requests);
    } catch (error) { console.error("İstatistik hatası:", error); }
}

function updateRecentActivity(borrows, requests) {
    const container = document.getElementById('recentActivity');
    let activities = [];
    if(borrows) borrows.forEach(b => activities.push({ type: 'borrow', date: b.borrowDate, title: b.book.title }));
    if(requests) requests.forEach(r => activities.push({ type: 'request', date: r.requestDate, title: r.book.title }));
    activities.sort((a, b) => new Date(b.date) - new Date(a.date));
    activities = activities.slice(0, 4);
    if (activities.length === 0) {
        container.innerHTML = '<small class="text-muted d-block text-center">İşlem yok.</small>';
        return;
    }
    let html = '';
    activities.forEach(act => {
        let icon = act.type === 'borrow' ? '<i class="fas fa-book text-primary"></i>' : '<i class="fas fa-paper-plane text-warning"></i>';
        html += `
        <div class="d-flex align-items-center gap-2 mb-2 p-2 rounded bg-white border-bottom">
            <div class="small">${icon}</div>
            <div style="font-size:12px; line-height:1.2;">
                <strong>${act.title}</strong><br>
                <span class="text-muted">${act.date}</span>
            </div>
        </div>`;
    });
    container.innerHTML = html;
}

function showError(container, error) {
    container.innerHTML = `<div class="text-danger text-center py-3">Veriler yüklenirken hata oluştu.<br><small>${error.message}</small></div>`;
}

async function borrowRequest(bookId) {
    try {
        await apiPost(`/user/borrow-request/${username}/${bookId}`, {});
        showToast("Başarılı", "İstek gönderildi!");
        updateStats();
    } catch (e) { showToast("Hata", e.message); }
}

async function returnBookViaList(id) {
    if(!confirm("İade etmek istiyor musunuz?")) return;
    try {
        await apiPost(`/user/return/username/${username}/${id}`, {});
        showToast("Başarılı", "İade edildi.");
        loadMyBooks();
        updateStats();
    } catch (e) { showToast("Hata", e.message); }
}

async function returnBook() {
    const id = document.getElementById('returnIdInput').value;
    if(id) returnBookViaList(id);
}

// PROFİL AYARLARI
async function openSettingsModal() {
    try {
        const user = await apiGet('/user/me');
        document.getElementById('reqUsername').value = user.username;
        document.getElementById('reqEmail').value = user.email;
        new bootstrap.Modal(document.getElementById('settingsModal')).show();
    } catch (e) { showToast('Hata', 'Bilgiler alınamadı.'); }
}

async function submitProfileRequest() {
    const data = {
        username: document.getElementById('reqUsername').value,
        email: document.getElementById('reqEmail').value
    };
    try {
        await apiPost('/user/settings/profile-request', data);
        showToast('Başarılı', 'İsteğiniz yöneticiye iletildi.');
        bootstrap.Modal.getInstance(document.getElementById('settingsModal')).hide();
    } catch(e) {
        showToast('Hata', e.message);
    }
}

function showToast(title, msg) {
    document.getElementById('toastTitle').innerText = title;
    document.getElementById('toastMessage').innerText = msg;
    new bootstrap.Toast(document.getElementById('liveToast')).show();
}