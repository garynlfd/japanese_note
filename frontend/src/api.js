const API_BASE = process.env.REACT_APP_API_URL || '/api';

function getToken() {
    return localStorage.getItem('token');
}

function setToken(token) {
    localStorage.setItem('token', token);
}

function removeToken() {
    localStorage.removeItem('token');
}

async function apiFetch(path, options = {}) {
    const token = getToken();
    const headers = { ...options.headers };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    if (options.body && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }

    const res = await fetch(`${API_BASE}${path}`, { ...options, headers });

    if (res.status === 401 || res.status === 403) {
        removeToken();
        window.location.reload();
        return null;
    }

    return res;
}

export { apiFetch, getToken, setToken, removeToken, API_BASE };
