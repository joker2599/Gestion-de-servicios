import { useEffect, useState } from 'react';
import type { FormEvent } from 'react';
import { PageHeader } from '../components/PageHeader';
import { clientesApi, usuariosApi } from '../services/api';
import type { Cliente, Usuario } from '../types';

const emptyForm = {
  usuarioId: '',
  razonSocial: '',
  documento: '',
  telefono: '',
  direccion: '',
  ciudad: '',
};

export function ClientesPage() {
  const [rows, setRows] = useState<Cliente[]>([]);
  const [usuarios, setUsuarios] = useState<Usuario[]>([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  async function reload() {
    const [clientes, users] = await Promise.all([clientesApi.list(), usuariosApi.list()]);
    setRows(clientes);
    setUsuarios(users.filter((u) => u.rol === 'CLIENTE'));
  }

  useEffect(() => {
    reload().catch(() => setError('No se pudieron cargar clientes.'));
  }, []);

  async function onSubmit(e: FormEvent) {
    e.preventDefault();
    setError('');
    setMessage('');
    try {
      const payload = {
        usuarioId: Number(form.usuarioId),
        razonSocial: form.razonSocial,
        documento: form.documento || undefined,
        telefono: form.telefono || undefined,
        direccion: form.direccion || undefined,
        ciudad: form.ciudad || undefined,
      };
      if (editingId) {
        await clientesApi.update(editingId, payload);
        setMessage('Cliente actualizado.');
      } else {
        await clientesApi.create(payload);
        setMessage('Cliente creado.');
      }
      setForm(emptyForm);
      setEditingId(null);
      await reload();
    } catch {
      setError('No se pudo guardar el cliente (¿usuario ya vinculado?).');
    }
  }

  function startEdit(c: Cliente) {
    setEditingId(c.id);
    setForm({
      usuarioId: String(c.usuarioId),
      razonSocial: c.razonSocial,
      documento: c.documento ?? '',
      telefono: c.telefono ?? '',
      direccion: c.direccion ?? '',
      ciudad: c.ciudad ?? '',
    });
  }

  async function onDelete(id: number) {
    if (!confirm('¿Eliminar cliente?')) return;
    try {
      await clientesApi.remove(id);
      await reload();
    } catch {
      setError('No se pudo eliminar.');
    }
  }

  return (
    <div>
      <PageHeader
        title="Clientes"
        subtitle="CRUD administrativo de perfiles de cliente."
      />
      {error ? <p className="mb-3 text-sm text-rose-600">{error}</p> : null}
      {message ? <p className="mb-3 text-sm text-accent-deep">{message}</p> : null}

      <form onSubmit={onSubmit} className="panel mb-6 grid gap-3 md:grid-cols-3">
        <select
          className="field"
          required
          value={form.usuarioId}
          onChange={(e) => setForm({ ...form, usuarioId: e.target.value })}
          disabled={!!editingId}
        >
          <option value="">Usuario CLIENTE…</option>
          {usuarios.map((u) => (
            <option key={u.id} value={u.id}>
              {u.username} ({u.email})
            </option>
          ))}
        </select>
        <input
          className="field"
          placeholder="Razón social"
          required
          value={form.razonSocial}
          onChange={(e) => setForm({ ...form, razonSocial: e.target.value })}
        />
        <input
          className="field"
          placeholder="Documento"
          value={form.documento}
          onChange={(e) => setForm({ ...form, documento: e.target.value })}
        />
        <input
          className="field"
          placeholder="Teléfono"
          value={form.telefono}
          onChange={(e) => setForm({ ...form, telefono: e.target.value })}
        />
        <input
          className="field"
          placeholder="Ciudad"
          value={form.ciudad}
          onChange={(e) => setForm({ ...form, ciudad: e.target.value })}
        />
        <input
          className="field md:col-span-3"
          placeholder="Dirección"
          value={form.direccion}
          onChange={(e) => setForm({ ...form, direccion: e.target.value })}
        />
        <div className="flex gap-2 md:col-span-3">
          <button className="btn-primary" type="submit">
            {editingId ? 'Guardar cambios' : 'Crear cliente'}
          </button>
          {editingId ? (
            <button
              type="button"
              className="btn-secondary"
              onClick={() => {
                setEditingId(null);
                setForm(emptyForm);
              }}
            >
              Cancelar
            </button>
          ) : null}
        </div>
      </form>

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Razón social</th>
              <th>Documento</th>
              <th>Ciudad</th>
              <th>Teléfono</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {rows.map((c) => (
              <tr key={c.id}>
                <td>{c.id}</td>
                <td>{c.razonSocial}</td>
                <td>{c.documento ?? '—'}</td>
                <td>{c.ciudad ?? '—'}</td>
                <td>{c.telefono ?? '—'}</td>
                <td className="space-x-2 text-right">
                  <button type="button" className="btn-secondary" onClick={() => startEdit(c)}>
                    Editar
                  </button>
                  <button type="button" className="btn-danger" onClick={() => onDelete(c.id)}>
                    Eliminar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
