import { createContext, useContext, useState } from "react";
import api from "../services/api";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem("bcp_user")); }
    catch { return null; }
  });

  const isAuthenticated = !!user;

  async function login(username, password) {
    const res = await api.post("/api/auth/login", { username, password });
    const data = res.data;
    localStorage.setItem("bcp_token", data.token);
    localStorage.setItem("bcp_user", JSON.stringify({ username: data.username, role: data.role }));
    setUser({ username: data.username, role: data.role });
    return data;
  }

  function logout() {
    localStorage.removeItem("bcp_token");
    localStorage.removeItem("bcp_user");
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, isAuthenticated, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
