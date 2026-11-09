import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { RolUsuario } from '../types';

export function ProtectedRoute({ roles }: { roles?: RolUsuario[] }) {
  const { session, loading, hasRole } = useAuth();

  if (loading) {
    return (
      <div className="grid min-h-screen place-items-center bg-mesh text-ink-600">
        Cargando sesión…
      </div>
    );
  }

  if (!session) return <Navigate to="/login" replace />;
  if (roles && !hasRole(...roles)) return <Navigate to="/" replace />;

  return <Outlet />;
}
