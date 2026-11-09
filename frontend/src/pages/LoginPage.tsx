import { useState, type FormEvent } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function LoginPage() {
  const { session, login, loading } = useAuth();
  const [username, setUsername] = useState('admin');
  const [password, setPassword] = useState('Admin123!');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  if (!loading && session) return <Navigate to="/" replace />;

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setSubmitting(true);
    try {
      await login(username, password);
    } catch {
      setError('Credenciales inválidas o backend no disponible.');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="relative grid min-h-screen place-items-center overflow-hidden bg-ink-950 px-4">
      <div className="pointer-events-none absolute inset-0 bg-[radial-gradient(circle_at_20%_20%,rgba(26,168,145,0.28),transparent_35%),radial-gradient(circle_at_80%_10%,rgba(201,120,22,0.18),transparent_30%),linear-gradient(160deg,#0b1220,#152038_55%,#0f7a6c33)]" />
      <div className="pointer-events-none absolute inset-0 opacity-[0.08] [background-image:linear-gradient(rgba(255,255,255,.35)_1px,transparent_1px),linear-gradient(90deg,rgba(255,255,255,.35)_1px,transparent_1px)] [background-size:42px_42px]" />

      <form
        onSubmit={onSubmit}
        className="relative z-10 w-full max-w-md rounded-2xl border border-white/10 bg-white/95 p-8 shadow-panel backdrop-blur"
      >
        <p className="font-display text-3xl font-semibold tracking-tight text-ink-950">
          TechService
        </p>
        <p className="mt-2 text-sm text-ink-500">
          Accede al panel de gestión de servicios técnicos.
        </p>

        <label className="mt-6 block text-xs font-semibold uppercase tracking-wide text-ink-500">
          Usuario
          <input
            className="field mt-1.5"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            required
          />
        </label>

        <label className="mt-4 block text-xs font-semibold uppercase tracking-wide text-ink-500">
          Contraseña
          <input
            type="password"
            className="field mt-1.5"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </label>

        {error ? <p className="mt-3 text-sm text-rose-600">{error}</p> : null}

        <button type="submit" className="btn-primary mt-6 w-full" disabled={submitting}>
          {submitting ? 'Entrando…' : 'Iniciar sesión'}
        </button>

        <div className="mt-5 rounded-lg bg-ink-50 p-3 text-xs text-ink-600">
          Demo: <code>admin / Admin123!</code> · <code>tecnico / Tecnico123!</code> ·{' '}
          <code>cliente / Cliente123!</code>
        </div>
      </form>
    </div>
  );
}
