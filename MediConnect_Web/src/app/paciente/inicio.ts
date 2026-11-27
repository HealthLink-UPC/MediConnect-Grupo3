import { Component, computed, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Receta, Solicitud } from '../core/models';
import { chipReceta, chipSolicitud, diasParaVencer, fechaCorta, mensajeError, nombreDeMedicamentos } from '../core/util';

/** Panel de inicio del paciente (Fig. 70/71): resumen, recetas por vencer y últimas solicitudes. */
@Component({
  selector: 'app-paciente-inicio',
  imports: [RouterLink],
  template: `
    <div class="pagina-cab row between wrap">
      <div>
        <div class="eyebrow">Panel del paciente</div>
        <h1>Hola, {{ auth.sesion()?.nombre_usuario }}</h1>
        <p>Aquí tienes el resumen de tus recetas y solicitudes.</p>
      </div>
      <a class="btn" routerLink="/paciente/solicitar">+ Solicitar receta</a>
    </div>

    @if (error()) { <div class="aviso error">{{ error() }}</div> }
    @if (cargando()) { <p class="muted">Cargando…</p> } @else {
      <div class="grid-4">
        <div class="card kpi"><span class="muted">Recetas vigentes</span><b>{{ vigentes() }}</b></div>
        <div class="card kpi"><span class="muted">Por vencer (15 días)</span><b>{{ porVencer().length }}</b></div>
        <div class="card kpi"><span class="muted">Solicitudes en curso</span><b>{{ enCurso() }}</b></div>
        <div class="card kpi"><span class="muted">Recetas emitidas</span><b>{{ recetas().length }}</b></div>
      </div>

      @if (porVencer().length) {
        <div class="aviso warn mt">
          <b>Atención:</b> {{ porVencer().length }} receta(s) vence(n) pronto. Puedes solicitar su renovación.
        </div>
      }

      <div class="grid-2 mt">
        <section class="card">
          <div class="row between"><h3>Mis recetas</h3><a routerLink="/paciente/recetas" class="small">Ver todas</a></div>
          @for (r of recetas().slice(0, 4); track r.id_receta) {
            <div class="item-med">
              <span class="icono-pastilla">💊</span>
              <div style="flex:1"><b>{{ nombres(r) }}</b>
                <div class="muted small">Vigente hasta {{ f(r.fecha_vencimiento) }}</div></div>
              <span class="chip {{ chip(r).clase }}">{{ chip(r).texto }}</span>
            </div>
          } @empty { <p class="muted mt">Aún no tienes recetas emitidas.</p> }
        </section>

        <section class="card">
          <div class="row between"><h3>Últimas solicitudes</h3><a routerLink="/paciente/solicitudes" class="small">Ver todas</a></div>
          @for (s of solicitudes().slice(0, 4); track s.id_solicitud) {
            <div class="item-med">
              <span class="icono-pastilla">▤</span>
              <div style="flex:1"><b>#{{ s.id_solicitud }} · {{ s.tipo_solicitud }}</b>
                <div class="muted small">{{ s.nombre_medico }} · {{ f(s.fecha_solicitud) }}</div></div>
              <span class="chip {{ chipS(s).clase }}">{{ chipS(s).texto }}</span>
            </div>
          } @empty { <p class="muted mt">Todavía no has hecho solicitudes.</p> }
        </section>
      </div>
    }
  `,
})
export class PacienteInicio {
  private api = inject(ApiService);
  auth = inject(AuthService);
  recetas = signal<Receta[]>([]);
  solicitudes = signal<Solicitud[]>([]);
  cargando = signal(true);
  error = signal('');
  f = fechaCorta; chip = chipReceta; chipS = chipSolicitud; nombres = nombreDeMedicamentos;

  porVencer = computed(() => this.recetas().filter(r => { const d = diasParaVencer(r); return d >= 0 && d <= 15; }));
  vigentes = computed(() => this.recetas().filter(r => diasParaVencer(r) >= 0).length);
  enCurso = computed(() => this.solicitudes().filter(s => s.estado_solicitud <= 2).length);

  constructor() {
    const id = this.auth.sesion()!.id_paciente!;
    forkJoin({ r: this.api.recetasDePaciente(id), s: this.api.solicitudesDePaciente(id) }).subscribe({
      next: ({ r, s }) => { this.recetas.set(r); this.solicitudes.set(s); this.cargando.set(false); },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
}
