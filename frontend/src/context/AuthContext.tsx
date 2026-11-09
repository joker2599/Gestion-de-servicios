import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react';
import { authApi, clearSession, loadSession, saveSession } from '../services/api';
import type { AuthSession, RolUsuario } from '../types';

interface AuthContextValue {
  session: AuthSession | null;
  loading: boolean;
  login: (username: string, password: string) => Promise<void>;
  logout: () => void;
  hasRole: (...roles: RolUsuario[]) => boolean;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [session, setSession] = useState<AuthSession | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const existing = loadSession();
    if (!existing) {
      setLoading(false);
      return;
    }
    authApi
      .me()
      .then(() => setSession(existing))
      .catch(() => {
        clearSession();
        setSession(null);
      })
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (username: string, password: string) => {
    const data = await authApi.login(username, password);
    const next: AuthSession = {
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      userId: data.userId,
      username: data.username,
      email: data.email,
      rol: data.rol,
    };
    saveSession(next);
    setSession(next);
  }, []);

  const logout = useCallback(() => {
    clearSession();
    setSession(null);
  }, []);

  const hasRole = useCallback(
    (...roles: RolUsuario[]) => !!session && roles.includes(session.rol),
    [session],
  );

  const value = useMemo(
    () => ({ session, loading, login, logout, hasRole }),
    [session, loading, login, logout, hasRole],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return ctx;
}
