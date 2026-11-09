import { useEffect, useMemo, useState } from 'react';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { ordenesApi } from '../services/api';
import type { OrdenServicio } from '../types';

export function ReportesPage() {
  const [ordenes, setOrdenes] = useState<OrdenServicio[]>([]);
  const [estado, setEstado] = useState('TODOS');
  const [prioridad, setPrioridad] = useState('TODAS');
  const [q, setQ] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    ordenesApi
      .list()
      .then(setOrdenes)
      .catch(() => setError('No se pudo cargar el reporte.'));
  }, []);

  const filtered = useMemo(() => {
    return ordenes.filter((o) => {
      if (estado !== 'TODOS' && o.estado !== estado) return false;
      if (prioridad !== 'TODAS' && o.prioridad !== prioridad) return false;
      if (q && !`${o.codigo} ${o.descripcion}`.toLowerCase().includes(q.toLowerCase())) {
        return false;
      }
      return true;
    });
  }, [ordenes, estado, prioridad, q]);

  const resumen = useMemo(() => {
    const total = filtered.length;
    const criticas = filtered.filter((o) => o.prioridad === 'CRITICA').length;
    const abiertas = filtered.filter((o) =>
      ['PENDIENTE', 'ASIGNADA', 'EN_PROCESO', 'ESPERA_REPUESTO'].includes(o.estado),
    ).length;
    return { total, criticas, abiertas };
  }, [filtered]);

  return (
    <div>
      <PageHeader
        title="Reportes"
        subtitle="Listado filtrable de servicios para seguimiento operativo."
      />
      {error ? <p className="mb-3 text-sm text-rose-600">{error}</p> : null}

      <div className="mb-4 grid gap-3 md:grid-cols-4">
        <input
          className="field md:col-span-2"
          placeholder="Buscar código o descripción…"
          value={q}
          onChange={(e) => setQ(e.target.value)}
        />
        <select className="field" value={estado} onChange={(e) => setEstado(e.target.value)}>
          <option value="TODOS">Todos los estados</option>
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
        <select className="field" value={prioridad} onChange={(e) => setPrioridad(e.target.value)}>
          <option value="TODAS">Todas las prioridades</option>
          {['BAJA', 'MEDIA', 'ALTA', 'CRITICA'].map((p) => (
            <option key={p} value={p}>
              {p}
            </option>
          ))}
        </select>
      </div>

      <div className="mb-4 grid gap-3 sm:grid-cols-3">
        <div className="panel">
          <p className="text-xs uppercase text-ink-500">Filtradas</p>
          <p className="font-display text-2xl font-semibold">{resumen.total}</p>
        </div>
        <div className="panel">
          <p className="text-xs uppercase text-ink-500">Abiertas</p>
          <p className="font-display text-2xl font-semibold">{resumen.abiertas}</p>
        </div>
        <div className="panel">
          <p className="text-xs uppercase text-ink-500">Críticas</p>
          <p className="font-display text-2xl font-semibold">{resumen.criticas}</p>
        </div>
      </div>

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Código</th>
              <th>Estado</th>
              <th>Prioridad</th>
              <th>Equipo</th>
              <th>Técnico</th>
              <th>Creada</th>
              <th>Cierre</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((o) => (
              <tr key={o.id}>
                <td className="font-semibold">{o.codigo}</td>
                <td>
                  <StatusBadge value={o.estado} />
                </td>
                <td>
                  <StatusBadge value={o.prioridad} />
                </td>
                <td>#{o.equipoId}</td>
                <td>{o.tecnicoId ?? '—'}</td>
                <td>{new Date(o.fechaCreacion).toLocaleString()}</td>
                <td>{o.fechaCierre ? new Date(o.fechaCierre).toLocaleString() : '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
