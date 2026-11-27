import { HttpErrorResponse } from '@angular/common/http';
import { Receta, Solicitud } from './models';

/** Convierte cualquier error HTTP del backend en un texto legible para el usuario. */
export function mensajeError(e: unknown): string {
  const err = e as HttpErrorResponse;
  if (err?.status === 0) return 'No se pudo conectar con el servidor. Verifica tu conexión e inténtalo de nuevo.';
  const b = err?.error;
  if (typeof b === 'string' && b) {
    try { return mensajeError({ status: err.status, error: JSON.parse(b) }); } catch { return b; }
  }
  if (b && typeof b === 'object') {
    if (b.error) return String(b.error);
    const vals = Object.values(b).filter(v => typeof v === 'string');
    if (vals.length) return vals.join(' · ');
  }
  return 'Ocurrió un error inesperado. Inténtalo nuevamente.';
}

/** Errores de validación por campo (400 con {campo: mensaje}). */
export function erroresPorCampo(e: unknown): Record<string, string> {
  const b = (e as HttpErrorResponse)?.error;
  if (b && typeof b === 'object' && !b.error) {
    const r: Record<string, string> = {};
    for (const [k, v] of Object.entries(b)) if (typeof v === 'string') r[k] = v;
    return r;
  }
  return {};
}

export function iniciales(nombre: string): string {
  const p = (nombre ?? '').trim().split(/\s+/).filter(Boolean);
  return ((p[0]?.[0] ?? '') + (p.length > 1 ? p[p.length - 1][0] : '')).toUpperCase();
}

export function fechaCorta(iso: string | null | undefined): string {
  if (!iso) return '—';
  const d = new Date(iso + 'T00:00:00');
  return d.toLocaleDateString('es-PE', { day: '2-digit', month: 'short', year: 'numeric' }).replace('.', '');
}

export function edad(iso: string | null | undefined): number | null {
  if (!iso) return null;
  const n = new Date(iso + 'T00:00:00'), h = new Date();
  let e = h.getFullYear() - n.getFullYear();
  if (h < new Date(h.getFullYear(), n.getMonth(), n.getDate())) e--;
  return e;
}

/** Fecha local (no UTC) en formato AAAA-MM-DD, para que «hoy» no se adelante por la zona horaria. */
export const aISO = (d: Date) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
export const hoyISO = () => aISO(new Date());

export type EstadoVisual = { clase: string; texto: string };

/** Solicitud: 1 Pendiente (ámbar) · 2 En revisión (azul) · 3 Emitida (verde) · 4 Rechazada (rojo). */
export function chipSolicitud(s: Solicitud | number): EstadoVisual {
  const e = typeof s === 'number' ? s : s.estado_solicitud;
  return ({
    1: { clase: 'chip-amber', texto: 'Pendiente' },
    2: { clase: 'chip-blue', texto: 'En revisión' },
    3: { clase: 'chip-green', texto: 'Emitida' },
    4: { clase: 'chip-red', texto: 'Rechazada' },
  } as Record<number, EstadoVisual>)[e] ?? { clase: 'chip-gray', texto: String(e) };
}

/** Receta: Vigente (verde) · Por vencer ≤ 15 días (ámbar) · Vencida (rojo), calculado con fecha_vencimiento. */
export function diasParaVencer(r: Receta): number {
  const v = new Date(r.fecha_vencimiento + 'T00:00:00').getTime();
  const h = new Date(hoyISO() + 'T00:00:00').getTime();
  return Math.round((v - h) / 86400000);
}
export function chipReceta(r: Receta): EstadoVisual {
  const d = diasParaVencer(r);
  if (d < 0) return { clase: 'chip-red', texto: 'Vencida' };
  if (d <= 15) return { clase: 'chip-amber', texto: 'Por vencer' };
  return { clase: 'chip-green', texto: 'Vigente' };
}
export function nombreDeMedicamentos(r: Receta): string {
  return r.detalles.map(d => `${d.nombre_medicamento} ${d.concentracion ?? ''}`.trim()).join(' · ');
}
