export type RolUsuario = 'ADMIN' | 'TECNICO' | 'CLIENTE';
export type EstadoUsuario = 'ACTIVO' | 'INACTIVO' | 'BLOQUEADO';
export type EstadoOrden =
  | 'PENDIENTE'
  | 'ASIGNADA'
  | 'EN_PROCESO'
  | 'ESPERA_REPUESTO'
  | 'COMPLETADA'
  | 'CANCELADA'
  | 'CERRADA';
export type PrioridadOrden = 'BAJA' | 'MEDIA' | 'ALTA' | 'CRITICA';
export type EstadoDisponibilidad = 'DISPONIBLE' | 'OCUPADO' | 'AUSENTE';

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresInMs: number;
  userId: number;
  username: string;
  email: string;
  rol: RolUsuario;
}

export interface Usuario {
  id: number;
  username: string;
  email: string;
  nombre: string;
  apellido?: string;
  rol: RolUsuario;
  estado: EstadoUsuario;
  creadoEn: string;
}

export interface Cliente {
  id: number;
  usuarioId: number;
  razonSocial: string;
  documento?: string;
  telefono?: string;
  direccion?: string;
  ciudad?: string;
}

export interface Tecnico {
  id: number;
  usuarioId: number;
  especialidad: string;
  disponibilidad: EstadoDisponibilidad;
  telefono?: string;
}

export interface Equipo {
  id: number;
  clienteId: number;
  tipo: string;
  marca: string;
  modelo: string;
  numeroSerie: string;
  descripcion?: string;
}

export interface OrdenServicio {
  id: number;
  codigo: string;
  descripcion: string;
  prioridad: PrioridadOrden;
  estado: EstadoOrden;
  equipoId: number;
  tecnicoId?: number | null;
  fechaCreacion: string;
  fechaAsignacion?: string | null;
  fechaCierre?: string | null;
}

export interface DetalleServicio {
  id: number;
  ordenServicioId: number;
  diagnostico?: string;
  solucion?: string;
  observaciones?: string;
  registradoEn: string;
}

export interface AuthSession {
  accessToken: string;
  refreshToken: string;
  userId: number;
  username: string;
  email: string;
  rol: RolUsuario;
}
