// Modelos que reflejan EXACTAMENTE el JSON (snake_case) que devuelve el backend Spring.

export type Rol = 'PACIENTE' | 'MEDICO';

export interface Sesion {
  token: string;
  id_usuario: number;
  rol_usuario: Rol;
  nombre_usuario: string;
  id_paciente: number | null;
  id_medico: number | null;
}

/** Respuesta de /auth/login: con la verificación en dos pasos activa trae id_reto y NO trae token todavía. */
export interface LoginRespuesta extends Partial<Sesion> {
  requiere_codigo?: boolean | null;
  id_reto?: string | null;
  correo_enmascarado?: string | null;
}

export interface RegistroBase {
  correo_usuario: string;
  clave_usuario: string;
  nombre_usuario: string;
  apellido_usuario: string;
  dni: string;
  fecha_nacimiento: string;
  telefono: string;
}
export interface RegistroMedico extends RegistroBase {
  especialidad: string;
  cmp: string;
  centro_atencion: string;
}

export interface MedicamentoSolicitado {
  id_medicamento: number;
  nombre_medicamento: string;
  concentracion: string;
}

export interface Solicitud {
  id_solicitud: number;
  fecha_solicitud: string;
  tipo_solicitud: string;
  motivo: string;
  motivo_estado: string | null;
  estado_solicitud: number; // 1 Pendiente, 2 En revisión, 3 Emitida, 4 Rechazada
  estado_nombre: string;
  id_paciente: number;
  nombre_paciente: string;
  id_medico: number;
  nombre_medico: string;
  id_receta: number | null;
  medicamentos: MedicamentoSolicitado[];
}

export interface DetalleReceta {
  id_tratamiento: number;
  nombre_medicamento: string;
  concentracion: string;
  dosis: string;
  frecuencia: string;
  fecha_inicio: string;
  fecha_fin: string;
}

export interface Receta {
  id_receta: number;
  fecha_emision: string;
  fecha_vencimiento: string;
  codigo_verificacion: string;
  estado_receta: number;
  estado_nombre: string;
  id_paciente: number;
  nombre_paciente: string;
  id_medico: number;
  nombre_medico: string;
  detalles: DetalleReceta[];
}

export interface FichaClinica {
  id_paciente: number;
  nombre_paciente: string;
  tipo_sangre: string | null;
  peso: number | null;
  talla: number | null;
  alergias: string[];
  enfermedades: string[];
  mensaje: string | null;
}

export interface Medico {
  id_medico: number;
  nombre_medico: string;
  especialidad: string;
  cmp: string;
  centro_atencion: string;
}

export interface Perfil {
  id_usuario: number;
  rol_usuario: string;
  nombre_usuario: string;
  apellido_usuario: string;
  dni: string;
  fecha_nacimiento: string;
  correo_usuario: string;
  telefono: string;
}

export interface Medicamento { id_medicamento: number; nombre_medicamento: string; concentracion: string; estado_medicamento: number; }
export interface Alergia { id_alergia: number; nombre_alergia: string; estado_alergia: number; }
export interface Enfermedad { id_enfermedad: number; nombre_enfermedad: string; estado_enfermedad: number; }

export interface ItemReceta {
  id_medicamento: number;
  dosis: string;
  frecuencia: string;
  fecha_inicio: string;
  fecha_fin: string;
}
