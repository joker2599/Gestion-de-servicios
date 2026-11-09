import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { equiposApi, ordenesApi, tecnicosApi } from '../services/api';
import type { DetalleServicio, Equipo, OrdenServicio, Tecnico } from '../types';

export function OrdenesPage() {
  const { hasRole } = useAuth();
  const [ordenes, setOrdenes] = useState<OrdenServicio[]>([]);
  const [equipos, setEquipos] = useState<Equipo[]>([]);
  const [tecnicos, setTecnicos] = useState<Tecnico[]>([]);
  const [selected, setSelected] = useState<OrdenServicio | null>(null);
  const [detalles, setDetalles] = useState<DetalleServicio[]>([]);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    equipoId: '',
    descripcion: '',
    prioridad: 'MEDIA',
    tecnicoId: '',
  });
  const [detalleForm, setDetalleForm] = useState({
    diagnostico: '',
    solucion: '',
    observaciones: '',
  });

  async function reload() {
    const [ords, eqs, tecs] = await Promise.all([
      ordenesApi.list(),
      equiposApi.list(),
      tecnicosApi.list().catch(() => [] as Tecnico[]),
    ]);
    setOrdenes(ords);
    setEquipos(eqs);
    setTecnicos(tecs);
  }

  useEffect(() => {
    reload().catch(() => setError('No se pudieron cargar las órdenes.'));
  }, []);

  async function openOrden(o: OrdenServicio) {
    setSelected(o);
    setDetalles(await ordenesApi.detalles(o.id));
  }

  async function onCreate(e: FormEvent) {
    e.preventDefault();
    setError('');
    try {
      await ordenesApi.create({
        equipoId: Number(form.equipoId),
        descripcion: form.descripcion,
        prioridad: form.prioridad,
        tecnicoId: form.tecnicoId ? Number(form.tecnicoId) : undefined,
      });
      setForm({ equipoId: '', descripcion: '', prioridad: 'MEDIA', tecnicoId: '' });
      await reload();
    } catch {
      setError('No se pudo crear la orden.');
    }
  }

  async function onAssign(ordenId: number, tecnicoId: number) {
    try {
      const updated = await ordenesApi.assign(ordenId, tecnicoId);
      await reload();
      setSelected(updated);
    } catch {
      setError('Asignación fallida.');
    }
  }

  async function onEstado(ordenId: number, estado: string) {
    try {
      const updated = await ordenesApi.updateEstado(ordenId, estado);
      await reload();
      setSelected(updated);
    } catch {
      setError('No se pudo actualizar el estado.');
    }
  }

  async function onDetalle(e: FormEvent) {
    e.preventDefault();
    if (!selected) return;
    try {
      await ordenesApi.addDetalle({
        ordenServicioId: selected.id,
        ...detalleForm,
      });
      setDetalleForm({ diagnostico: '', solucion: '', observaciones: '' });
      const updatedList = await ordenesApi.list();
      setOrdenes(updatedList);
      const refreshed = updatedList.find((o) => o.id === selected.id) ?? selected;
      setSelected(refreshed);
      setDetalles(await ordenesApi.detalles(selected.id));
    } catch {
      setError('No se pudo registrar el detalle.');
    }
  }

  return (
    <div>
      <PageHeader
        title="Órdenes de servicio"
        subtitle="Crear, asignar y actualizar según tu rol."
      />
      {error ? <p className="mb-3 text-sm text-rose-600">{error}</p> : null}

      {hasRole('ADMIN', 'CLIENTE') ? (
        <form onSubmit={onCreate} className="panel mb-6 grid gap-3 md:grid-cols-2">
          <select
            className="field"
            required
            value={form.equipoId}
            onChange={(e) => setForm({ ...form, equipoId: e.target.value })}
          >
            <option value="">Equipo…</option>
            {equipos.map((eq) => (
              <option key={eq.id} value={eq.id}>
                {eq.numeroSerie} — {eq.marca} {eq.modelo}
              </option>
            ))}
          </select>
          <select
            className="field"
            value={form.prioridad}
            onChange={(e) => setForm({ ...form, prioridad: e.target.value })}
          >
            {['BAJA', 'MEDIA', 'ALTA', 'CRITICA'].map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
          {hasRole('ADMIN') ? (
            <select
              className="field"
              value={form.tecnicoId}
              onChange={(e) => setForm({ ...form, tecnicoId: e.target.value })}
            >
              <option value="">Sin técnico (asignar después)</option>
              {tecnicos.map((t) => (
                <option key={t.id} value={t.id}>
                  #{t.id} · {t.especialidad}
                </option>
              ))}
            </select>
          ) : null}
          <textarea
            className="field md:col-span-2 min-h-24"
            required
            placeholder="Descripción del problema"
            value={form.descripcion}
            onChange={(e) => setForm({ ...form, descripcion: e.target.value })}
          />
          <button className="btn-primary md:w-fit" type="submit">
            Crear orden
          </button>
        </form>
      ) : null}

      <div className="grid gap-6 lg:grid-cols-5">
        <div className="table-wrap lg:col-span-3">
          <table className="data-table">
            <thead>
              <tr>
                <th>Código</th>
                <th>Prioridad</th>
                <th>Estado</th>
                <th>Equipo</th>
                <th>Técnico</th>
              </tr>
            </thead>
            <tbody>
              {ordenes.map((o) => (
                <tr key={o.id} className={selected?.id === o.id ? 'bg-accent-soft/40' : undefined}>
                  <td>
                    <button
                      type="button"
                      className="font-semibold text-accent-deep hover:underline"
                      onClick={() => openOrden(o)}
                    >
                      {o.codigo}
                    </button>
                  </td>
                  <td>
                    <StatusBadge value={o.prioridad} />
                  </td>
                  <td>
                    <StatusBadge value={o.estado} />
                  </td>
                  <td>#{o.equipoId}</td>
                  <td>{o.tecnicoId ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <section className="panel lg:col-span-2 space-y-4">
          {!selected ? (
            <p className="text-sm text-ink-500">Selecciona una orden para gestionarla.</p>
          ) : (
            <>
              <div>
                <h2 className="font-display text-lg font-semibold">{selected.codigo}</h2>
                <p className="mt-1 text-sm text-ink-600">{selected.descripcion}</p>
              </div>

              {hasRole('ADMIN') ? (
                <label className="block text-xs font-semibold uppercase text-ink-500">
                  Asignar técnico
                  <select
                    className="field mt-1"
                    defaultValue=""
                    onChange={(e) => {
                      if (e.target.value) onAssign(selected.id, Number(e.target.value));
                    }}
                  >
                    <option value="">Elegir…</option>
                    {tecnicos.map((t) => (
                      <option key={t.id} value={t.id}>
                        #{t.id} · {t.especialidad} ({t.disponibilidad})
                      </option>
                    ))}
                  </select>
                </label>
              ) : null}

              <label className="block text-xs font-semibold uppercase text-ink-500">
                Cambiar estado
                <select
                  className="field mt-1"
                  value={selected.estado}
                  onChange={(e) => onEstado(selected.id, e.target.value)}
                >
                  {[
                    'PENDIENTE',
                    'ASIGNADA',
                    'EN_PROCESO',
                    'ESPERA_REPUESTO',
                    'COMPLETADA',
                    'CANCELADA',
                    'CERRADA',
                  ].map((s) => (
                    <option key={s} value={s}>
                      {s}
                    </option>
                  ))}
                </select>
              </label>

              {hasRole('ADMIN', 'TECNICO') ? (
                <form onSubmit={onDetalle} className="space-y-2 border-t border-ink-200 pt-3">
                  <p className="text-xs font-semibold uppercase text-ink-500">Nuevo detalle</p>
                  <input
                    className="field"
                    placeholder="Diagnóstico"
                    value={detalleForm.diagnostico}
                    onChange={(e) =>
                      setDetalleForm({ ...detalleForm, diagnostico: e.target.value })
                    }
                  />
                  <input
                    className="field"
                    placeholder="Solución"
                    value={detalleForm.solucion}
                    onChange={(e) => setDetalleForm({ ...detalleForm, solucion: e.target.value })}
                  />
                  <input
                    className="field"
                    placeholder="Observaciones"
                    value={detalleForm.observaciones}
                    onChange={(e) =>
                      setDetalleForm({ ...detalleForm, observaciones: e.target.value })
                    }
                  />
                  <button className="btn-primary" type="submit">
                    Registrar actividad
                  </button>
                </form>
              ) : null}

              <div>
                <p className="text-xs font-semibold uppercase text-ink-500">Historial</p>
                <ul className="mt-2 space-y-2">
                  {detalles.length === 0 ? (
                    <li className="text-sm text-ink-500">Sin detalles.</li>
                  ) : (
                    detalles.map((d) => (
                      <li key={d.id} className="rounded-lg bg-white px-3 py-2 text-sm">
                        <p>{d.diagnostico || d.solucion || d.observaciones}</p>
                        <p className="text-xs text-ink-400">
                          {new Date(d.registradoEn).toLocaleString()}
                        </p>
                      </li>
                    ))
                  )}
                </ul>
              </div>
            </>
          )}
        </section>
      </div>
    </div>
  );
}
