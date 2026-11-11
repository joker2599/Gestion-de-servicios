import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { apiErrorMessage, ordenesApi, tecnicosApi, usuariosApi } from '../services/api';
import type { OrdenServicio, Tecnico, Usuario } from '../types';

const emptyNuevo = {
  username: '',
  email: '',
  password: '',
  nombre: '',
  apellido: '',
  especialidad: '',
  telefono: '',
  disponibilidad: 'DISPONIBLE',
};

export function TecnicosPage() {
  const { hasRole } = useAuth();
  const [tecnicos, setTecnicos] = useState<Tecnico[]>([]);
  const [ordenes, setOrdenes] = useState<OrdenServicio[]>([]);
  const [usuariosLibres, setUsuariosLibres] = useState<Usuario[]>([]);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [modo, setModo] = useState<'nuevo' | 'existente'>('nuevo');
  const [formNuevo, setFormNuevo] = useState(emptyNuevo);
  const [formExistente, setFormExistente] = useState({
    usuarioId: '',
    especialidad: '',
    telefono: '',
    disponibilidad: 'DISPONIBLE',
  });

  async function reload() {
    const [tecs, ords] = await Promise.all([tecnicosApi.list(), ordenesApi.list()]);
    setTecnicos(tecs);
    setOrdenes(ords);
    if (hasRole('ADMIN')) {
      const [users, libresIds] = await Promise.all([
        usuariosApi.list(),
        tecnicosApi.disponiblesUsuarioIds(),
      ]);
      const libres = new Set(libresIds);
      setUsuariosLibres(users.filter((u) => u.rol === 'TECNICO' && libres.has(u.id)));
    }
  }

  useEffect(() => {
    reload().catch(() => setError('No se pudo cargar técnicos.'));
  }, []);

  const workload = useMemo(() => {
    const map = new Map<number, number>();
    for (const o of ordenes) {
      if (!o.tecnicoId) continue;
      if (['COMPLETADA', 'CERRADA', 'CANCELADA'].includes(o.estado)) continue;
      map.set(o.tecnicoId, (map.get(o.tecnicoId) ?? 0) + 1);
    }
    return map;
  }, [ordenes]);

  async function onCreateNuevo(e: FormEvent) {
    e.preventDefault();
    setError('');
    setMessage('');
    try {
      await tecnicosApi.registro({
        username: formNuevo.username.trim(),
        email: formNuevo.email.trim(),
        password: formNuevo.password,
        nombre: formNuevo.nombre.trim(),
        apellido: formNuevo.apellido.trim() || undefined,
        especialidad: formNuevo.especialidad.trim(),
        telefono: formNuevo.telefono.trim() || undefined,
        disponibilidad: formNuevo.disponibilidad,
      });
      setFormNuevo(emptyNuevo);
      setMessage('Técnico creado (usuario + perfil).');
      await reload();
    } catch (err) {
      setError(apiErrorMessage(err, 'No se pudo crear el técnico.'));
    }
  }

  async function onCreateExistente(e: FormEvent) {
    e.preventDefault();
    setError('');
    setMessage('');
    try {
      await tecnicosApi.create({
        usuarioId: Number(formExistente.usuarioId),
        especialidad: formExistente.especialidad.trim(),
        telefono: formExistente.telefono.trim() || undefined,
        disponibilidad: formExistente.disponibilidad,
      });
      setFormExistente({
        usuarioId: '',
        especialidad: '',
        telefono: '',
        disponibilidad: 'DISPONIBLE',
      });
      setMessage('Perfil de técnico vinculado al usuario.');
      await reload();
    } catch (err) {
      setError(apiErrorMessage(err, 'No se pudo crear el técnico.'));
    }
  }

  async function onDisponibilidad(t: Tecnico, disponibilidad: string) {
    try {
      await tecnicosApi.update(t.id, {
        usuarioId: t.usuarioId,
        especialidad: t.especialidad,
        telefono: t.telefono,
        disponibilidad,
      });
      await reload();
    } catch (err) {
      setError(apiErrorMessage(err, 'No se pudo actualizar disponibilidad.'));
    }
  }

  async function onDelete(t: Tecnico) {
    const abiertas = workload.get(t.id) ?? 0;
    const hint =
      abiertas > 0
        ? `\nTiene ${abiertas} orden(es) abierta(s); el API rechazará el borrado hasta reasignar/cerrar.`
        : '';
    if (!confirm(`¿Eliminar el técnico #${t.id} (${t.especialidad})?${hint}`)) return;
    setError('');
    setMessage('');
    try {
      await tecnicosApi.remove(t.id);
      setMessage(`Técnico #${t.id} eliminado.`);
      await reload();
    } catch (err) {
      setError(apiErrorMessage(err, 'No se pudo eliminar el técnico.'));
    }
  }

  return (
    <div>
      <PageHeader
        title="Técnicos"
        subtitle="Gestión de perfiles y carga de trabajo activa."
      />
      {error ? <p className="mb-3 text-sm text-rose-600">{error}</p> : null}
      {message ? <p className="mb-3 text-sm text-emerald-700">{message}</p> : null}

      {hasRole('ADMIN') ? (
        <div className="panel mb-6 space-y-4">
          <div className="flex flex-wrap gap-2">
            <button
              type="button"
              className={modo === 'nuevo' ? 'btn-primary' : 'btn-secondary'}
              onClick={() => setModo('nuevo')}
            >
              Nuevo usuario + técnico
            </button>
            <button
              type="button"
              className={modo === 'existente' ? 'btn-primary' : 'btn-secondary'}
              onClick={() => setModo('existente')}
            >
              Vincular usuario TECNICO libre
            </button>
          </div>

          {modo === 'nuevo' ? (
            <form onSubmit={onCreateNuevo} className="grid gap-3 md:grid-cols-3">
              <input
                className="field"
                required
                placeholder="Username"
                value={formNuevo.username}
                onChange={(e) => setFormNuevo({ ...formNuevo, username: e.target.value })}
              />
              <input
                className="field"
                required
                type="email"
                placeholder="Email"
                value={formNuevo.email}
                onChange={(e) => setFormNuevo({ ...formNuevo, email: e.target.value })}
              />
              <input
                className="field"
                required
                type="password"
                minLength={6}
                placeholder="Password (mín. 6)"
                value={formNuevo.password}
                onChange={(e) => setFormNuevo({ ...formNuevo, password: e.target.value })}
              />
              <input
                className="field"
                required
                placeholder="Nombre"
                value={formNuevo.nombre}
                onChange={(e) => setFormNuevo({ ...formNuevo, nombre: e.target.value })}
              />
              <input
                className="field"
                placeholder="Apellido"
                value={formNuevo.apellido}
                onChange={(e) => setFormNuevo({ ...formNuevo, apellido: e.target.value })}
              />
              <input
                className="field"
                required
                placeholder="Especialidad"
                value={formNuevo.especialidad}
                onChange={(e) => setFormNuevo({ ...formNuevo, especialidad: e.target.value })}
              />
              <input
                className="field"
                placeholder="Teléfono"
                value={formNuevo.telefono}
                onChange={(e) => setFormNuevo({ ...formNuevo, telefono: e.target.value })}
              />
              <select
                className="field"
                value={formNuevo.disponibilidad}
                onChange={(e) => setFormNuevo({ ...formNuevo, disponibilidad: e.target.value })}
              >
                {['DISPONIBLE', 'OCUPADO', 'AUSENTE'].map((d) => (
                  <option key={d} value={d}>
                    {d}
                  </option>
                ))}
              </select>
              <button className="btn-primary" type="submit">
                Crear técnico
              </button>
            </form>
          ) : (
            <form onSubmit={onCreateExistente} className="grid gap-3 md:grid-cols-4">
              <select
                className="field"
                required
                value={formExistente.usuarioId}
                onChange={(e) => setFormExistente({ ...formExistente, usuarioId: e.target.value })}
              >
                <option value="">Usuario TECNICO libre…</option>
                {usuariosLibres.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.username} ({u.email})
                  </option>
                ))}
              </select>
              <input
                className="field"
                required
                placeholder="Especialidad"
                value={formExistente.especialidad}
                onChange={(e) => setFormExistente({ ...formExistente, especialidad: e.target.value })}
              />
              <input
                className="field"
                placeholder="Teléfono"
                value={formExistente.telefono}
                onChange={(e) => setFormExistente({ ...formExistente, telefono: e.target.value })}
              />
              <button className="btn-primary" type="submit" disabled={usuariosLibres.length === 0}>
                Vincular perfil
              </button>
              {usuariosLibres.length === 0 ? (
                <p className="md:col-span-4 text-sm text-slate-600">
                  No hay usuarios TECNICO sin perfil. Usa «Nuevo usuario + técnico» o crea antes un
                  usuario con rol TECNICO.
                </p>
              ) : null}
            </form>
          )}
        </div>
      ) : null}

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Especialidad</th>
              <th>Disponibilidad</th>
              <th>Órdenes abiertas</th>
              <th>Teléfono</th>
              {hasRole('ADMIN', 'TECNICO') ? <th>Acciones</th> : null}
            </tr>
          </thead>
          <tbody>
            {tecnicos.map((t) => (
              <tr key={t.id}>
                <td>#{t.id}</td>
                <td>{t.especialidad}</td>
                <td>
                  <StatusBadge value={t.disponibilidad} />
                </td>
                <td>
                  <span className="font-semibold">{workload.get(t.id) ?? 0}</span>
                </td>
                <td>{t.telefono ?? '—'}</td>
                {hasRole('ADMIN', 'TECNICO') ? (
                  <td className="space-x-2 text-right whitespace-nowrap">
                    <select
                      className="field inline-block w-auto"
                      value={t.disponibilidad}
                      onChange={(e) => onDisponibilidad(t, e.target.value)}
                    >
                      {['DISPONIBLE', 'OCUPADO', 'AUSENTE'].map((d) => (
                        <option key={d} value={d}>
                          {d}
                        </option>
                      ))}
                    </select>
                    {hasRole('ADMIN') ? (
                      <button type="button" className="btn-danger" onClick={() => onDelete(t)}>
                        Eliminar
                      </button>
                    ) : null}
                  </td>
                ) : null}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
