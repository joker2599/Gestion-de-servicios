import axios from 'axios';
import type {
  AuthResponse,
  Cliente,
  DetalleServicio,
  Equipo,
  OrdenServicio,
  Tecnico,
  Usuario,
} from '../types';

const baseURL = import.meta.env.VITE_API_URL || '';

export const api = axios.create({
  baseURL,
  headers: { 'Content-Type': 'application/json' },
});

const STORAGE_KEY = 'tsm.auth';

export function loadSession(): import('../types').AuthSession | null {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) return null;
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function saveSession(session: import('../types').AuthSession) {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(session));
}

export function clearSession() {
  localStorage.removeItem(STORAGE_KEY);
}

api.interceptors.request.use((config) => {
  const session = loadSession();
  if (session?.accessToken) {
    config.headers.Authorization = `Bearer ${session.accessToken}`;
  }
  return config;
});

type RetryConfig = { _retry?: boolean };

api.interceptors.response.use(
  (res) => res,
  async (error) => {
    const original = error.config as (typeof error.config & RetryConfig) | undefined;
    if (error.response?.status === 401 && original && !original._retry) {
      original._retry = true;
      const session = loadSession();
      if (session?.refreshToken) {
        try {
          const { data } = await axios.post<AuthResponse>(
            `${baseURL}/api/v1/auth/refresh`,
            { refreshToken: session.refreshToken },
          );
          const next = {
            accessToken: data.accessToken,
            refreshToken: data.refreshToken,
            userId: data.userId,
            username: data.username,
            email: data.email,
            rol: data.rol,
          };
          saveSession(next);
          original.headers.Authorization = `Bearer ${data.accessToken}`;
          return api(original);
        } catch {
          clearSession();
        }
      } else {
        clearSession();
      }
    }
    return Promise.reject(error);
  },
);

export const authApi = {
  login: async (username: string, password: string) => {
    const { data } = await api.post<AuthResponse>('/api/v1/auth/login', { username, password });
    return data;
  },
  me: async () => {
    const { data } = await api.get<Usuario>('/api/v1/auth/me');
    return data;
  },
};

export const clientesApi = {
  list: async () => (await api.get<Cliente[]>('/api/v1/clientes')).data,
  create: async (body: Omit<Cliente, 'id'> & { usuarioId: number }) =>
    (await api.post<Cliente>('/api/v1/clientes', body)).data,
  update: async (id: number, body: Omit<Cliente, 'id'>) =>
    (await api.put<Cliente>(`/api/v1/clientes/${id}`, body)).data,
  remove: async (id: number) => api.delete(`/api/v1/clientes/${id}`),
};

export function apiErrorMessage(error: unknown, fallback: string): string {
  if (typeof error === 'object' && error !== null && 'response' in error) {
    const data = (error as { response?: { data?: { message?: string; details?: string[] } } }).response?.data;
    if (data?.details?.length) {
      return `${data.message ?? fallback}: ${data.details.join('; ')}`;
    }
    if (data?.message) return data.message;
  }
  return fallback;
}

export const tecnicosApi = {
  list: async () => (await api.get<Tecnico[]>('/api/v1/tecnicos')).data,
  disponiblesUsuarioIds: async () =>
    (await api.get<number[]>('/api/v1/tecnicos/disponibles-usuario-ids')).data,
  create: async (body: {
    usuarioId: number;
    especialidad: string;
    disponibilidad?: string;
    telefono?: string;
  }) => (await api.post<Tecnico>('/api/v1/tecnicos', body)).data,
  /** Alta atómica: Usuario TECNICO + perfil técnico */
  registro: async (body: {
    username: string;
    email: string;
    password: string;
    nombre: string;
    apellido?: string;
    especialidad: string;
    disponibilidad?: string;
    telefono?: string;
  }) => (await api.post<Tecnico>('/api/v1/tecnicos/registro', body)).data,
  update: async (
    id: number,
    body: { usuarioId: number; especialidad: string; disponibilidad?: string; telefono?: string },
  ) => (await api.put<Tecnico>(`/api/v1/tecnicos/${id}`, body)).data,
  remove: async (id: number) => api.delete(`/api/v1/tecnicos/${id}`),
};

export const equiposApi = {
  list: async (clienteId?: number) =>
    (await api.get<Equipo[]>('/api/v1/equipos', { params: clienteId ? { clienteId } : undefined })).data,
  create: async (body: Omit<Equipo, 'id'>) => (await api.post<Equipo>('/api/v1/equipos', body)).data,
  update: async (id: number, body: Omit<Equipo, 'id'>) =>
    (await api.put<Equipo>(`/api/v1/equipos/${id}`, body)).data,
  remove: async (id: number) => api.delete(`/api/v1/equipos/${id}`),
};

export const ordenesApi = {
  list: async () => (await api.get<OrdenServicio[]>('/api/v1/ordenes')).data,
  create: async (body: {
    equipoId: number;
    descripcion: string;
    prioridad?: string;
    tecnicoId?: number;
  }) => (await api.post<OrdenServicio>('/api/v1/ordenes', body)).data,
  assign: async (id: number, tecnicoId: number) =>
    (await api.post<OrdenServicio>(`/api/v1/ordenes/${id}/asignar-tecnico`, { tecnicoId })).data,
  updateEstado: async (id: number, estado: string) =>
    (await api.patch<OrdenServicio>(`/api/v1/ordenes/${id}/estado`, { estado })).data,
  detalles: async (id: number) =>
    (await api.get<DetalleServicio[]>(`/api/v1/ordenes/${id}/detalles`)).data,
  addDetalle: async (body: {
    ordenServicioId: number;
    diagnostico?: string;
    solucion?: string;
    observaciones?: string;
  }) => (await api.post<DetalleServicio>('/api/v1/ordenes/detalles', body)).data,
  remove: async (id: number) => api.delete(`/api/v1/ordenes/${id}`),
};

export const usuariosApi = {
  list: async () => (await api.get<Usuario[]>('/api/v1/usuarios')).data,
  create: async (body: {
    username: string;
    email: string;
    password: string;
    nombre: string;
    apellido?: string;
    rol: string;
  }) => (await api.post<Usuario>('/api/v1/usuarios', body)).data,
};
