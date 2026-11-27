import { Component, computed, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { iniciales } from '../core/util';

interface Enlace { ruta: string; texto: string; icono: string; }

/** Barra lateral azul + cabecera. Menú por rol según el informe (4.2.3). */
@Component({
  selector: 'app-portal',
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  template: `
    <div class="portal">
      <aside class="lateral">
        <div class="auth-logo"><img src="img/favicon.png" alt=""> MediConnect</div>
        <span class="badge-portal">{{ auth.esMedico() ? '🩺 Portal Médico' : '🩺 Portal Paciente' }}</span>
        <nav aria-label="Navegación principal">
          @for (e of enlaces(); track e.ruta) {
            <a [routerLink]="e.ruta" routerLinkActive="activo"><span aria-hidden="true">{{ e.icono }}</span>{{ e.texto }}</a>
          }
          <a (click)="auth.cerrar()" role="button" tabindex="0" (keydown.enter)="auth.cerrar()"><span aria-hidden="true">⎋</span>Cerrar sesión</a>
        </nav>
        <div class="ayuda"><b>¿Necesitas ayuda?</b>Estamos disponibles de lunes a viernes, 8:00-18:00.</div>
      </aside>
      <div class="contenido">
        <header class="topbar">
          <span class="muted small">Atención segura · Datos protegidos</span>
          <div class="user">
            <span class="avatar">{{ ini() }}</span>
            <span><b>{{ nombre() }}</b><span class="muted small">{{ auth.esMedico() ? 'Médico' : 'Paciente' }}</span></span>
          </div>
        </header>
        <main class="main"><router-outlet /></main>
      </div>
    </div>
  `,
})
export class PortalLayout {
  auth = inject(AuthService);
  nombre = computed(() => {
    const n = this.auth.sesion()?.nombre_usuario ?? '';
    return this.auth.esMedico() ? `Dr(a). ${n}` : n;
  });
  ini = computed(() => iniciales(this.auth.sesion()?.nombre_usuario ?? ''));
  enlaces = computed<Enlace[]>(() => this.auth.esMedico()
    ? [
        { ruta: '/medico/bandeja', texto: 'Bandeja', icono: '▤' },
        { ruta: '/medico/pacientes', texto: 'Pacientes', icono: '☺' },
        { ruta: '/medico/recetas', texto: 'Recetas emitidas', icono: '▧' },
        { ruta: '/medico/catalogo', texto: 'Catálogo', icono: '☰' },
        { ruta: '/medico/perfil', texto: 'Mi perfil', icono: '✚' },
      ]
    : [
        { ruta: '/paciente/inicio', texto: 'Inicio', icono: '⌂' },
        { ruta: '/paciente/recetas', texto: 'Mis recetas', icono: '▧' },
        { ruta: '/paciente/solicitudes', texto: 'Solicitudes', icono: '▤' },
        { ruta: '/paciente/historial', texto: 'Mi historial', icono: '◷' },
        { ruta: '/paciente/perfil', texto: 'Mi perfil', icono: '☺' },
      ]);
}
