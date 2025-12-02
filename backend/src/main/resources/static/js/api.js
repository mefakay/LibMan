
 //  ORTAK API YARDIMCI FONKSİYONLAR

const API_BASE = 'http://localhost:8080/api';


async function apiRequest(endpoint, options = {}) {
    const url = `${API_BASE}${endpoint}`;
    const defaultOptions = {
        headers: {
            'Content-Type': 'application/json',
            ...options.headers
        }
    };

    const config = { ...defaultOptions, ...options };

    try {
        const response = await fetch(url, config);

        const text = await response.text();
        
        // JSON parse etmeye çalış, başarısız olursa text olarak kullan
        let data;
        try {
            data = text ? JSON.parse(text) : null;
        } catch (e) {
            data = text;
        }

        if (!response.ok) {
            // Validation hatalarını handle et
            if (data && typeof data === 'object' && data.errors) {
                // tüm hataları birleştir
                const errorMessages = Object.values(data.errors).join(', ');
                throw new Error(errorMessages || data.message || 'Validation hatası');
            }
            throw new Error(typeof data === 'string' ? data : (data && data.message ? data.message : 'Bir hata oluştu'));
        }

        return data;
    } catch (error) {
        throw error;
    }
}

async function apiGet(endpoint) {
    return apiRequest(endpoint, { method: 'GET' });
}

async function apiPost(endpoint, body) {
    return apiRequest(endpoint, {
        method: 'POST',
        body: JSON.stringify(body)
    });
}

async function apiPut(endpoint, body) {
    return apiRequest(endpoint, {
        method: 'PUT',
        body: JSON.stringify(body)
    });
}

async function apiDelete(endpoint) {
    return apiRequest(endpoint, { method: 'DELETE' });
}


function showToast(message, type = 'info', duration = 4000) {
    // Toast container'ı oluştur (yoksa)
    let container = document.getElementById('toast-container');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toast-container';
        document.body.appendChild(container);
    }
    
    // Toast elementi oluştur
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;

    const icons = {
        success: '✅',
        error: '❌',
        info: 'ℹ️'
    };

    toast.innerHTML = `
        <span class="toast-icon">${icons[type] || icons.info}</span>
        <span class="toast-message">${message}</span>
        <button class="toast-close" onclick="this.parentElement.remove()">×</button>
    `;

    container.appendChild(toast);
    
    // Otomatik kaldır
    setTimeout(() => {
        toast.classList.add('hiding');
        setTimeout(() => {
            if (toast.parentElement) {
                toast.remove();
            }
        }, 300);
    }, duration);
}

