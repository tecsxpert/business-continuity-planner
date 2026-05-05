import axios from "axios";

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080",
});

// Attach JWT token to every request (set by AuthContext after login)
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("bcp_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// On 401 — clear stored credentials and redirect to login
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem("bcp_token");
      localStorage.removeItem("bcp_user");
      window.location.href = "/login";
    }
    return Promise.reject(err);
  }
);

export default api;
