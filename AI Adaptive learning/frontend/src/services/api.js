import axios from 'axios';

// Use environment variable if available, otherwise default to backend on port 8080
// Can override with: VITE_API_BASE=http://localhost:<port>/api
const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('sf_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default api;
