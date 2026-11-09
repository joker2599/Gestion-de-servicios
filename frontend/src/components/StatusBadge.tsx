const ESTADO_STYLES: Record<string, string> = {
  PENDIENTE: 'bg-warn-soft text-warn',
  ASIGNADA: 'bg-sky-100 text-sky-800',
  EN_PROCESO: 'bg-accent-soft text-accent-deep',
  ESPERA_REPUESTO: 'bg-amber-100 text-amber-800',
  COMPLETADA: 'bg-emerald-100 text-emerald-800',
  CANCELADA: 'bg-rose-100 text-rose-800',
  CERRADA: 'bg-ink-100 text-ink-700',
  DISPONIBLE: 'bg-accent-soft text-accent-deep',
  OCUPADO: 'bg-warn-soft text-warn',
  AUSENTE: 'bg-ink-100 text-ink-600',
  BAJA: 'bg-ink-100 text-ink-600',
  MEDIA: 'bg-sky-100 text-sky-800',
  ALTA: 'bg-warn-soft text-warn',
  CRITICA: 'bg-rose-100 text-rose-800',
};

export function StatusBadge({ value }: { value: string }) {
  return (
    <span
      className={`inline-flex rounded-md px-2 py-0.5 text-xs font-semibold tracking-wide ${
        ESTADO_STYLES[value] ?? 'bg-ink-100 text-ink-700'
      }`}
    >
      {value.replaceAll('_', ' ')}
    </span>
  );
}
