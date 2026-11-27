import { Injectable, computed, signal } from '@angular/core';
import { Router } from '@angular/router';
import { inject } from '@angular/core';
import { Sesion } from './models';

const CLAVE = 'mediconnect.sesion';

/** Guarda la sesión (token JWT + datos del usuario). "Recordarme" → localStorage; si no, sessionStorage. */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private router = inject(Router);
  readonly sesion = signal<Sesion | null>(this.leer());
  readonly esPaciente = computed(() => this.sesion()?.rol_usuario === 'PACIENTE');
  readonly esMedico = computed(() => this.sesion()?.rol_usuario === 'MEDICO');
  /** Inicio de sesión que espera el código de verificación enviado al correo. */
  readonly reto = signal<{ id: string; correo: string } | null>(null);
  readonly token = computed(() => this.sesion()?.token ?? null);

  private leer(): Sesion | null {
    try {
      const txt = sessionStorage.getItem(CLAVE) ?? localStorage.getItem(CLAVE);
      if (!txt) return null;
      const s = JSON.parse(txt) as Sesion;
      return this.vencido(s.token) ? null : s;
    } catch { return null; }
  }

  private vencido(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return payload.exp * 1000 < Date.now();
    } catch { return true; }
  }

  iniciar(s: Sesion, recordar: boolean) {
    try {
      sessionStorage.removeItem(CLAVE); localStorage.removeItem(CLAVE);
      (recordar ? localStorage : sessionStorage).setItem(CLAVE, JSON.stringify(s));
    } catch { /* almacenamiento bloqueado: la sesión vive solo en memoria */ }
    this.sesion.set(s);
  }

  cerrar(vencida = false) {
    this.reto.set(null);
    try { sessionStorage.removeItem(CLAVE); localStorage.removeItem(CLAVE); } catch { /* nada */ }
    this.sesion.set(null);
    this.router.navigate(['/login'], vencida ? { queryParams: { vencida: 1 } } : {});
  }

  inicio(): string {
    return this.esMedico() ? '/medico/bandeja' : '/paciente/inicio';
  }
}
