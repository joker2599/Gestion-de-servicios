import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { PageHeader } from '../components/PageHeader';
import { StatusBadge } from '../components/StatusBadge';
import { useAuth } from '../context/AuthContext';
import { ordenesApi, tecnicosApi } from '../services/api';
import type { OrdenServicio, Tecnico } from '../types';

export function DashboardPage() {
  const { session, hasRole } = useAuth();
  const [ordenes, setOrdenes] = useState<OrdenServicio[]>([]);
  const [tecnicos, setTecnicos] = useState<Tecnico[]>([]);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([ordenesApi.list(), tecnicosApi.list().catch(() => [] as Tecnico[])])
      .then(([ords, tecs]) => {
        setOrdenes(ords);
        setTecnicos(tecs);
      })
      .catch(() => setError('No se pudieron cargar las métricas.'));
  }, []);

  const stats = useMemo(() => {
    const byEstado: Record<string, number> = {};
    for (const o of ordenes) byEstado[o.estado] = (byEstado[o.estado] ?? 0) + 1;
    const abiertas = ordenes.filter((o) =>
      ['PENDIENTE', 'ASIGNADA', 'EN_PROCESO', 'ESPERA_REPUESTO'].includes(o.estado),
    ).length;
    const cerradas = ordenes.filter((o) =>
      ['COMPLETADA', 'CERRADA'].includes(o.estado),
    );
    const avgHours =
      cerradas.length === 0
        ? null
        : cerradas.reduce((acc, o) => {
            if (!o.fechaCierre) return acc;
            const ms =
              new Date(o.fechaCierre).getTime() - new Date(o.fechaCreacion).getTime();
            return acc + ms / 36e5;
          }, 0) / cerradas.length;

    return { byEstado, abiertas, avgHours, total: ordenes.length };
  }, [ordenes]);

  const recientes = [...ordenes]
    .sort((a, b) => +new Date(b.fechaCreacion) - +new Date(a.fechaCreacion))
    .slice(0, 6);

  return (
    <div>
      <PageHeader
        title="Dashboard"
        subtitle={`Hola ${session?.username}. Vista ${session?.rol.toLowerCase()} del taller.`}
        actions={
          hasRole('ADMIN', 'CLIENTE') ? (
            <Link to="/ordenes" className="btn-primary">
              Nueva orden
            </Link>
          ) : null
        }
      />

      {error ? <p className="mb-4 text-sm text-rose-600">{error}</p> : null}

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <Metric label="Órdenes totales" value={String(stats.total)} />
        <Metric label="Abiertas / en curso" value={String(stats.abiertas)} tone="warn" />
        <Metric
          label="Tiempo medio cierre"
          value={stats.avgHours == null ? '—' : `${stats.avgHours.toFixed(1)} h`}
        />
        <Metric
          label="Técnicos visibles"
          value={String(tecnicos.length)}
          tone="accent"
        />
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-2">
        <section className="panel">
          <h2 className="font-display text-sm font-semibold uppercase tracking-wide text-ink-600">
            Órdenes por estado
          </h2>
          <ul className="mt-3 space-y-2">
            {Object.keys(stats.byEstado).length === 0 ? (
              <li className="text-sm text-ink-500">Sin órdenes todavía.</li>
            ) : (
              Object.entries(stats.byEstado).map(([estado, count]) => (
                <li key={estado} className="flex items-center justify-between text-sm">
                  <StatusBadge value={estado} />
                  <span className="font-semibold text-ink-800">{count}</span>
                </li>
              ))
            )}
          </ul>
        </section>

        <section className="panel">
          <h2 className="font-display text-sm font-semibold uppercase tracking-wide text-ink-600">
            Actividad reciente
          </h2>
          <div className="mt-3 space-y-2">
            {recientes.length === 0 ? (
              <p className="text-sm text-ink-500">No hay actividad para mostrar.</p>
            ) : (
              recientes.map((o) => (
                <div
                  key={o.id}
                  className="flex items-center justify-between gap-3 rounded-lg bg-white px-3 py-2"
                >
                  <div>
                    <p className="text-sm font-semibold text-ink-900">{o.codigo}</p>
                    <p className="line-clamp-1 text-xs text-ink-500">{o.descripcion}</p>
                  </div>
                  <StatusBadge value={o.estado} />
                </div>
              ))
            )}
          </div>
        </section>
      </div>
    </div>
  );
}

function Metric({
  label,
  value,
  tone = 'ink',
}: {
  label: string;
  value: string;
  tone?: 'ink' | 'warn' | 'accent';
}) {
  const toneClass =
    tone === 'warn'
      ? 'border-warn/30 bg-warn-soft'
      : tone === 'accent'
        ? 'border-accent/30 bg-accent-soft'
        : 'border-ink-200 bg-white';
  return (
    <div className={`rounded-xl border p-4 ${toneClass}`}>
      <p className="text-xs font-semibold uppercase tracking-wide text-ink-500">{label}</p>
      <p className="mt-2 font-display text-3xl font-semibold text-ink-950">{value}</p>
    </div>
  );
}
