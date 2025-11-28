/* ============================================
   ORTAK API YARDIMCI FONKSİYONLAR
   ============================================ */

const API_BASE = 'http://localhost:8080/api';

/**
 * API isteği yapar
 * @param {string} endpoint - API endpoint'i
 * @param {object} options - Fetch options (method, body, headers)
 * @returns {Promise} Response promise
 */
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
        const data = await response.json().catch(() => response.text());
        
        if (!response.ok) {
            throw new Error(typeof data === 'string' ? data : (data.message || 'Bir hata oluştu'));
        }
        
        return data;
    } catch (error) {
        throw error;
    }
}

/**
 * GET isteği yapar
 */
async function apiGet(endpoint) {
    return apiRequest(endpoint, { method: 'GET' });
}

/**
 * POST isteği yapar
 */
async function apiPost(endpoint, body) {
    return apiRequest(endpoint, {
        method: 'POST',
        body: JSON.stringify(body)
    });
}

/**
 * PUT isteği yapar
 */
async function apiPut(endpoint, body) {
    return apiRequest(endpoint, {
        method: 'PUT',
        body: JSON.stringify(body)
    });
}

/**
 * DELETE isteği yapar
 */
async function apiDelete(endpoint) {
    return apiRequest(endpoint, { method: 'DELETE' });
}

