import { createContext, useContext, useMemo, useState } from 'react';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(() => {
    const token = localStorage.getItem('sf_token');
    const user = localStorage.getItem('sf_user');
    return {
      token,
      user: user ? JSON.parse(user) : null
    };
  });

  const login = (payload) => {
    localStorage.setItem('sf_token', payload.token);
    localStorage.setItem('sf_user', JSON.stringify({
      userId: payload.userId,
      name: payload.name,
      email: payload.email,
      role: payload.role
    }));

    setAuth({
      token: payload.token,
      user: {
        userId: payload.userId,
        name: payload.name,
        email: payload.email,
        role: payload.role
      }
    });
  };

  const logout = () => {
    localStorage.removeItem('sf_token');
    localStorage.removeItem('sf_user');
    setAuth({ token: null, user: null });
  };

  const value = useMemo(() => ({ auth, login, logout }), [auth]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  return useContext(AuthContext);
}
