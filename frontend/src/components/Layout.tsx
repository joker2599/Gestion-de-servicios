import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { RolUsuario } from '../types';

interface NavItem {
  to: string;
  label: string;
  roles?: RolUsuario[];
}

const NAV: NavItem[] = [
  { to: '/', label: 'Dashboard' },
  { to: '/clientes', label: 'Clientes', roles: ['ADMIN'] },
  { to: '/equipos', label: 'Equipos' },
  { to: '/ordenes', label: 'Órdenes' },
  { to: '/tecnicos', label: 'Técnicos' },
  { to: '/reportes', label: 'Reportes', roles: ['ADMIN', 'TECNICO'] },
];

export function Layout() {
  const { session, logout, hasRole } = useAuth();

  const items = NAV.filter((item) => !item.roles || hasRole(...item.roles));

  return (
    <div className="min-h-screen bg-mesh">
      <div className="mx-auto flex min-h-screen max-w-7xl gap-6 px-4 py-5 md:px-6">
        <aside className="hidden w-60 shrink-0 flex-col rounded-2xl bg-ink-900 p-5 text-ink-100 shadow-panel md:flex">
          <div className="mb-8">
            <p className="font-display text-lg font-semibold tracking-tight text-white">
              TechService
            </p>
            <p className="mt-1 text-xs text-ink-400">Gestión de servicios técnicos</p>
          </div>
          <nav className="flex flex-1 flex-col gap-1">
            {items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  `rounded-lg px-3 py-2 text-sm font-medium transition ${
                    isActive
                      ? 'bg-accent text-white'
                      : 'text-ink-300 hover:bg-ink-800 hover:text-white'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="mt-6 border-t border-ink-800 pt-4">
            <p className="text-sm font-medium text-white">{session?.username}</p>
            <p className="text-xs uppercase tracking-wide text-accent-glow">{session?.rol}</p>
            <button
              type="button"
              onClick={logout}
              className="mt-3 w-full rounded-lg border border-ink-700 px-3 py-2 text-left text-sm text-ink-300 transition hover:border-warn hover:text-warn"
            >
              Cerrar sesión
            </button>
          </div>
        </aside>

        <div className="flex min-w-0 flex-1 flex-col">
          <header className="mb-4 flex items-center justify-between rounded-2xl border border-ink-200/80 bg-white/80 px-4 py-3 backdrop-blur md:hidden">
            <div>
              <p className="font-display font-semibold text-ink-900">TechService</p>
              <p className="text-xs text-ink-500">
                {session?.username} · {session?.rol}
              </p>
            </div>
            <button
              type="button"
              onClick={logout}
              className="rounded-lg bg-ink-900 px-3 py-1.5 text-xs text-white"
            >
              Salir
            </button>
          </header>

          <nav className="mb-4 flex gap-2 overflow-x-auto md:hidden">
            {items.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/'}
                className={({ isActive }) =>
                  `whitespace-nowrap rounded-full px-3 py-1.5 text-xs font-medium ${
                    isActive ? 'bg-accent text-white' : 'bg-white text-ink-700'
                  }`
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <main className="flex-1 rounded-2xl border border-ink-200/70 bg-white/90 p-5 shadow-panel backdrop-blur md:p-7">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
}
