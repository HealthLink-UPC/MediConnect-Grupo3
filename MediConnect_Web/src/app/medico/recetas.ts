import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Receta, Solicitud } from '../core/models';
import { fechaCorta, mensajeError } from '../core/util';
import { RecetaModal } from '../shared/receta-modal';

/** Recetas emitidas (Figs. 102–103): las solicitudes aprobadas por este médico; el detalle trae la receta completa. */
@Component({
  selector: 'app-medico-recetas',
  imports: [FormsModule, RecetaModal],
  template: `
    <div class="pagina-cab"><h1>Recetas emitidas</h1><p>Registro de las recetas que has emitido.</p></div>
    <div class="filtros">
      <input class="input" placeholder="Buscar paciente…" [ngModel]="texto()" (ngModelChange)="texto.set($event)" aria-label="Buscar paciente">
      <input class="input" type="date" [ngModel]="fecha()" (ngModelChange)="fecha.set($event)" aria-label="Fecha" style="max-width:190px;flex:none">
    </div>
    @if (error()) { <div class="aviso error">{{ error() }}</div> }
    @if (cargando()) { <p class="muted">Cargando…</p> }
    @else if (!filtradas().length) { <div class="vacio"><b>Sin recetas emitidas</b>Las recetas que apruebes aparecerán aquí.</div> }
    @else {
      <div class="tabla-wrap"><table class="tabla">
        <thead><tr><th>Receta</th><th>Paciente</th><th>Solicitud</th><th>Fecha de solicitud</th><th></th></tr></thead>
        <tbody>
          @for (s of filtradas(); track s.id_solicitud) {
            <tr class="click" (click)="ver(s)">
              <td>#{{ s.id_receta }}</td><td><b>{{ s.nombre_paciente }}</b></td><td>#{{ s.id_solicitud }} · {{ s.tipo_solicitud }}</td>
              <td>{{ f(s.fecha_solicitud) }}</td><td style="text-align:right"><span class="btn sec peq">Ver receta</span></td>
            </tr>
          }
        </tbody>
      </table></div>
    }
    @if (receta(); as r) { <app-receta-modal [r]="r" (cerrar)="receta.set(null)" /> }
  `,
})
export class MedicoRecetas {
  private api = inject(ApiService);
  emitidas = signal<Solicitud[]>([]);
  receta = signal<Receta | null>(null);
  cargando = signal(true);
  error = signal('');
  texto = signal(''); fecha = signal('');
  f = fechaCorta;
  filtradas = computed(() => {
    const t = this.texto().trim().toLowerCase();
    return this.emitidas().filter(s => (!t || s.nombre_paciente.toLowerCase().includes(t)) && (!this.fecha() || s.fecha_solicitud === this.fecha()));
  });

  constructor() {
    this.api.solicitudesDeMedico(inject(AuthService).sesion()!.id_medico!, 3).subscribe({
      next: l => { this.emitidas.set(l.filter(s => s.id_receta)); this.cargando.set(false); },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  ver(s: Solicitud) {
    this.api.receta(s.id_receta!).subscribe({ next: r => this.receta.set(r), error: e => this.error.set(mensajeError(e)) });
  }
}
