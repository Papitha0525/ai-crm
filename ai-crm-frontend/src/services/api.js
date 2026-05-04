import axios from 'axios';

const api = axios.create({
    baseURL: 'http://localhost:8080',
    headers: {
        'Content-Type': 'application/json'
    }
});

// JWT token add பண்ணு
api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token && token !== 'admin-token') {
        config.headers.Authorization = 
            `Bearer ${token}`;
    }
    return config;
});

export default api;