import { useEffect, useMemo, useState } from 'react';
import type { FormEvent } from 'react';
import { PageHeader } from '../components/PageHeader';
import { useAuth } from '../context/AuthContext';
import { clientesApi, equiposApi, ordenesApi } from '../services/api';
import type { Cliente, Equipo, OrdenServicio } from '../types';
import { StatusBadge } from '../components/StatusBadge';

const empty = {
  clienteId: '',
  tipo: '',
  marca: '',
  modelo: '',
  numeroSerie: '',
  descripcion: '',
};

export function EquiposPage() {
  const { hasRole } = useAuth();
  const canWrite = hasRole('ADMIN', 'CLIENTE');
  const [equipos, setEquipos] = useState<Equipo[]>([]);
  const [clientes, setClientes] = useState<Cliente[]>([]);
  const [ordenes, setOrdenes] = useState<OrdenServicio[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState(empty);
  const [error, setError] = useState('');

  async function reload() {
    const [eq, ords] = await Promise.all([equiposApi.list(), ordenesApi.list()]);
    setEquipos(eq);
    setOrdenes(ords);
    if (hasRole('ADMIN', 'CLIENTE')) {
      try {
        setClientes(await clientesApi.list());
      } catch {
        setClientes([]);
      }
    }
  }

  useEffect(() => {
    reload().catch(() => setError('No se pudo cargar el inventario.'));
  }, []);

  const historial = useMemo(
    () =>
      selectedId == null
        ? []
        : ordenes
            .filter((o) => o.equipoId === selectedId)
            .sort((a, b) => +new Date(b.fechaCreacion) - +new Date(a.fechaCreacion)),
    [ordenes, selectedId],
  );

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    try {
      await equiposApi.create({
        clienteId: Number(form.clienteId || clientes[0]?.id || 0),
        tipo: form.tipo,
        marca: form.marca,
        modelo: form.modelo,
        numeroSerie: form.numeroSerie,
        descripcion: form.descripcion || undefined,
      });
      setForm(empty);
      await reload();
    } catch {
      setError('No se pudo registrar el equipo.');
    }
  }

  async function onDelete(id: number) {
    if (!confirm('¿Eliminar equipo?')) return;
    try {
      await equiposApi.remove(id);
      if (selectedId === id) setSelectedId(null);
      await reload();
    } catch {
      setError('No se pudo eliminar (¿tiene órdenes?).');
    }
  }

  return (
    <div>
      <PageHeader
        title="Equipos"
        subtitle="Inventario de equipos e historial de servicio asociado."
      />
      {error ? <p className="mb-3 text-sm text-rose-600">{error}</p> : null}

      {canWrite ? (
        <form onSubmit={onSubmit} className="panel mb-6 grid gap-3 md:grid-cols-3">
          {hasRole('ADMIN') ? (
            <select
              className="field"
              required
              value={form.clienteId}
              onChange={(e) => setForm({ ...form, clienteId: e.target.value })}
            >
              <option value="">Cliente…</option>
              {clientes.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.razonSocial}
                </option>
              ))}
            </select>
          ) : (
            <input type="hidden" value={form.clienteId} />
          )}
          <input
            className="field"
            placeholder="Tipo"
            required
            value={form.tipo}
            onChange={(e) => setForm({ ...form, tipo: e.target.value })}
          />
          <input
            className="field"
            placeholder="Marca"
            required
            value={form.marca}
            onChange={(e) => setForm({ ...form, marca: e.target.value })}
          />
          <input
            className="field"
            placeholder="Modelo"
            required
            value={form.modelo}
            onChange={(e) => setForm({ ...form, modelo: e.target.value })}
          />
          <input
            className="field"
            placeholder="Número de serie"
            required
            value={form.numeroSerie}
            onChange={(e) => setForm({ ...form, numeroSerie: e.target.value })}
          />
          <input
            className="field md:col-span-3"
            placeholder="Descripción"
            value={form.descripcion}
            onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
          />
          <button className="btn-primary md:col-span-3 md:w-fit" type="submit">
            Registrar equipo
          </button>
        </form>
      ) : null}

      <div className="grid gap-6 lg:grid-cols-5">
        <div className="table-wrap lg:col-span-3">
          <table className="data-table">
            <thead>
              <tr>
                <th>Serie</th>
                <th>Equipo</th>
                <th>Cliente</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {equipos.map((eq) => (
                <tr
                  key={eq.id}
                  className={selectedId === eq.id ? 'bg-accent-soft/40' : undefined}
                >
                  <td>
                    <button
                      type="button"
                      className="font-semibold text-accent-deep underline-offset-2 hover:underline"
                      onClick={() => setSelectedId(eq.id)}
                    >
                      {eq.numeroSerie}
                    </button>
                  </td>
                  <td>
                    {eq.marca} {eq.modelo}
                    <div className="text-xs text-ink-500">{eq.tipo}</div>
                  </td>
                  <td>#{eq.clienteId}</td>
                  <td className="text-right">
                    {canWrite ? (
                      <button type="button" className="btn-danger" onClick={() => onDelete(eq.id)}>
                        Eliminar
                      </button>
                    ) : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <section className="panel lg:col-span-2">
          <h2 className="font-display text-sm font-semibold uppercase tracking-wide text-ink-600">
            Historial de servicio
          </h2>
          {selectedId == null ? (
            <p className="mt-3 text-sm text-ink-500">Selecciona un equipo para ver su historial.</p>
          ) : historial.length === 0 ? (
            <p className="mt-3 text-sm text-ink-500">Sin órdenes para este equipo.</p>
          ) : (
            <ul className="mt-3 space-y-2">
              {historial.map((o) => (
                <li key={o.id} className="rounded-lg bg-white px-3 py-2">
                  <div className="flex items-center justify-between gap-2">
                    <span className="text-sm font-semibold">{o.codigo}</span>
                    <StatusBadge value={o.estado} />
                  </div>
                  <p className="mt-1 text-xs text-ink-500">{o.descripcion}</p>
                </li>
              ))}
            </ul>
          )}
        </section>
      </div>
    </div>
  );
}
