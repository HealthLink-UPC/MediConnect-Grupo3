import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin, map, of, switchMap } from 'rxjs';
import { environment } from './environment';
import {
  Alergia, Enfermedad, FichaClinica, ItemReceta, LoginRespuesta, Medicamento, Medico, Perfil, Receta,
  RegistroBase, RegistroMedico, Sesion, Solicitud,
} from './models';

/** El backend devuelve tipo/estado en MAYÚSCULAS (NUEVA_RECETA, PENDIENTE…); aquí se dejan legibles para mostrarlos. */
const legible = (t: string) => { const s = (t ?? '').toLowerCase().replace(/_/g, ' ').replace('renovacion', 'renovación'); return s.charAt(0).toUpperCase() + s.slice(1); };
const norm = (s: Solicitud): Solicitud => ({ ...s, tipo_solicitud: legible(s.tipo_solicitud), estado_nombre: legible(s.estado_nombre) });
const normL = (l: Solicitud[]) => l.map(norm);

/** Un único servicio con todas las llamadas al backend (rutas tal cual los controladores Spring). */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);
  private u = environment.apiUrl;

  // ----- Autenticación -----
  login(correo_usuario: string, clave_usuario: string): Observable<LoginRespuesta> {
    return this.http.post<LoginRespuesta>(`${this.u}/auth/login`, { correo_usuario, clave_usuario });
  }
  /** Segundo paso: el código de 6 dígitos que llegó al correo. Devuelve la sesión con el token. */
  verificarCodigo(id_reto: string, codigo: string): Observable<Sesion> {
    return this.http.post<Sesion>(`${this.u}/auth/login/verificar`, { id_reto, codigo });
  }
  reenviarCodigo(id_reto: string): Observable<LoginRespuesta> {
    return this.http.post<LoginRespuesta>(`${this.u}/auth/login/reenviar`, { id_reto });
  }
  /** Recuperar contraseña, paso 1: envía un código al correo. */
  recuperar(correo_usuario: string) { return this.http.post<LoginRespuesta>(`${this.u}/auth/recuperar`, { correo_usuario }); }
  /** Recuperar contraseña, paso 2: comprueba el código del correo. */
  verificarRecuperacion(id_reto: string, codigo: string) {
    return this.http.post(`${this.u}/auth/recuperar/verificar`, { id_reto, codigo }, { responseType: 'text' });
  }
  /** Recuperar contraseña, paso 3: código del correo + nueva contraseña. */
  restablecer(id_reto: string, codigo: string, clave_nueva: string) {
    return this.http.post(`${this.u}/auth/recuperar/restablecer`, { id_reto, codigo, clave_nueva }, { responseType: 'text' });
  }
  registrarPaciente(d: RegistroBase) {
    return this.http.post(`${this.u}/auth/register/paciente`, d, { responseType: 'text' });
  }
  registrarMedico(d: RegistroMedico) {
    return this.http.post(`${this.u}/auth/register/medico`, d, { responseType: 'text' });
  }

  // ----- Catálogos -----
  medicamentos() { return this.http.get<Medicamento[]>(`${this.u}/catalogo/medicamentos`); }
  alergias() { return this.http.get<Alergia[]>(`${this.u}/catalogo/alergias`); }
  enfermedades() { return this.http.get<Enfermedad[]>(`${this.u}/catalogo/enfermedades`); }
  medicos() { return this.http.get<Medico[]>(`${this.u}/medicos/listar`); }

  guardarMedicamento(nombre_medicamento: string, concentracion: string) {
    return this.http.post<Medicamento>(`${this.u}/catalogo/medicamentos`, { nombre_medicamento, concentracion, estado_medicamento: 1 });
  }
  guardarAlergia(nombre_alergia: string) {
    return this.http.post<Alergia>(`${this.u}/catalogo/alergias`, { nombre_alergia, estado_alergia: 1 });
  }
  guardarEnfermedad(nombre_enfermedad: string) {
    return this.http.post<Enfermedad>(`${this.u}/catalogo/enfermedades`, { nombre_enfermedad, estado_enfermedad: 1 });
  }
  eliminarMedicamento(id: number) { return this.http.delete(`${this.u}/catalogo/medicamentos/${id}`, { responseType: 'text' }); }
  eliminarAlergia(id: number) { return this.http.delete(`${this.u}/catalogo/alergias/${id}`, { responseType: 'text' }); }
  eliminarEnfermedad(id: number) { return this.http.delete(`${this.u}/catalogo/enfermedades/${id}`, { responseType: 'text' }); }

  // ----- Perfil -----
  perfil(id: number) { return this.http.get<Perfil>(`${this.u}/usuarios/${id}`); }
  actualizarPerfil(id: number, p: Perfil) { return this.http.put<Perfil>(`${this.u}/usuarios/${id}`, p); }
  cambiarClave(id: number, clave_actual: string, clave_nueva: string) {
    return this.http.put(`${this.u}/usuarios/${id}/clave`, { clave_actual, clave_nueva }, { responseType: 'text' });
  }
  darDeBaja(id: number) { return this.http.delete(`${this.u}/usuarios/${id}`, { responseType: 'text' }); }

  // ----- Ficha clínica -----
  ficha(idPaciente: number) { return this.http.get<FichaClinica>(`${this.u}/pacientes/${idPaciente}/ficha`); }
  guardarDatosClinicos(idPaciente: number, d: { tipo_sangre: string; peso: number | null; talla: number | null }) {
    return this.http.put<FichaClinica>(`${this.u}/pacientes/${idPaciente}/datos-clinicos`, d);
  }
  agregarAlergia(idPaciente: number, id_alergia: number) {
    return this.http.post<FichaClinica>(`${this.u}/pacientes/${idPaciente}/alergias`, { id_alergia });
  }
  quitarAlergia(idPaciente: number, idAlergia: number) {
    return this.http.delete<FichaClinica>(`${this.u}/pacientes/${idPaciente}/alergias/${idAlergia}`);
  }
  agregarEnfermedad(idPaciente: number, id_enfermedad: number) {
    return this.http.post<FichaClinica>(`${this.u}/pacientes/${idPaciente}/enfermedades`, { id_enfermedad });
  }
  quitarEnfermedad(idPaciente: number, idEnfermedad: number) {
    return this.http.delete<FichaClinica>(`${this.u}/pacientes/${idPaciente}/enfermedades/${idEnfermedad}`);
  }

  // ----- Recetas -----
  /** El listado del backend no trae los medicamentos; se completan pidiendo el detalle de cada receta. */
  recetasDePaciente(idPaciente: number): Observable<Receta[]> {
    return this.http.get<Receta[]>(`${this.u}/recetas/paciente/${idPaciente}`).pipe(
      switchMap(l => l.length ? forkJoin(l.map(r => this.receta(r.id_receta))) : of([] as Receta[])),
      map(l => l.sort((x, y) => y.id_receta - x.id_receta)),
    );
  }
  receta(id: number) { return this.http.get<Receta>(`${this.u}/recetas/${id}`); }

  // ----- Solicitudes -----
  solicitarNuevaReceta(id_medico: number, motivo: string, id_medicamentos: number[]) {
    return this.http.post<Solicitud>(`${this.u}/solicitudes/nueva-receta`, { id_medico, motivo, id_medicamentos }).pipe(map(norm));
  }
  solicitarRenovacion(id_receta: number, motivo: string) {
    return this.http.post<Solicitud>(`${this.u}/solicitudes/renovacion`, { id_receta, motivo }).pipe(map(norm));
  }
  solicitudesDePaciente(idPaciente: number) { return this.http.get<Solicitud[]>(`${this.u}/solicitudes/paciente/${idPaciente}`).pipe(map(normL)); }
  solicitudesDeMedico(idMedico: number, estado?: number) {
    const q = estado ? `?estado=${estado}` : '';
    return this.http.get<Solicitud[]>(`${this.u}/solicitudes/medico/${idMedico}${q}`).pipe(map(normL));
  }
  marcarEnRevision(idSolicitud: number) { return this.http.put<Solicitud>(`${this.u}/solicitudes/${idSolicitud}/revision`, {}).pipe(map(norm)); }
  aprobar(idSolicitud: number, detalles: ItemReceta[]) {
    return this.http.put<Receta>(`${this.u}/solicitudes/${idSolicitud}/aprobar`, { detalles });
  }
  rechazar(idSolicitud: number, motivo_rechazo: string) {
    return this.http.put<Solicitud>(`${this.u}/solicitudes/${idSolicitud}/rechazar`, { motivo_rechazo }).pipe(map(norm));
  }
}
