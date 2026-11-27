import { Component, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Receta, Solicitud } from '../core/models';
import { chipReceta, chipSolicitud, fechaCorta, mensajeError, nombreDeMedicamentos } from '../core/util';
import { RecetaModal } from '../shared/receta-modal';

/** Mi historial (Figs. 79–80): solo el registro de recetas y solicitudes (la ficha clínica vive en Mi perfil). */
@Component({
  selector: 'app-paciente-historial',
  imports: [RecetaModal],
  template: `
    <div class="pagina-cab"><h1>Mi historial</h1><p>El registro de tus recetas y solicitudes.</p></div>
    @if (error()) { <div class="aviso error">{{ error() }}</div> }
    @if (cargando()) { <p class="muted">Cargando…</p> } @else {
      <section class="card">
        <h3>Recetas emitidas</h3>
        @for (r of recetas(); track r.id_receta) {
          <div class="item-med" style="cursor:pointer" (click)="abierta.set(r)">
            <span class="icono-pastilla">💊</span>
            <div style="flex:1"><b>#{{ r.id_receta }} · {{ nombres(r) }}</b><div class="muted small">Emitida el {{ f(r.fecha_emision) }} por {{ r.nombre_medico }}</div></div>
            <span class="chip {{ chip(r).clase }}">{{ chip(r).texto }}</span>
          </div>
        } @empty { <p class="muted mt">Sin recetas todavía.</p> }
      </section>

      <section class="card">
        <h3>Historial de solicitudes</h3>
        @for (s of solicitudes(); track s.id_solicitud) {
          <div class="item-med">
            <span class="icono-pastilla">▤</span>
            <div style="flex:1"><b>#{{ s.id_solicitud }} · {{ s.tipo_solicitud }}</b>
              <div class="muted small">{{ f(s.fecha_solicitud) }} · {{ s.nombre_medico }}
                @if (s.estado_solicitud === 4 && s.motivo_estado) { · Motivo: {{ s.motivo_estado }} }</div></div>
            <span class="chip {{ chipS(s).clase }}">{{ chipS(s).texto }}</span>
          </div>
        } @empty { <p class="muted mt">Sin solicitudes todavía.</p> }
      </section>
    }
    @if (abierta(); as r) { <app-receta-modal [r]="r" (cerrar)="abierta.set(null)" /> }
  `,
})
export class PacienteHistorial {
  private api = inject(ApiService);
  idPaciente = inject(AuthService).sesion()!.id_paciente!;
  recetas = signal<Receta[]>([]);
  solicitudes = signal<Solicitud[]>([]);
  abierta = signal<Receta | null>(null);
  cargando = signal(true);
  error = signal('');
  f = fechaCorta; chip = chipReceta; chipS = chipSolicitud; nombres = nombreDeMedicamentos;

  constructor() {
    forkJoin({ r: this.api.recetasDePaciente(this.idPaciente), s: this.api.solicitudesDePaciente(this.idPaciente) }).subscribe({
      next: ({ r, s }) => { this.recetas.set(r); this.solicitudes.set(s); this.cargando.set(false); },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
}
