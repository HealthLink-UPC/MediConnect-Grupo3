import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Receta, Solicitud } from '../core/models';
import { chipSolicitud, fechaCorta, mensajeError } from '../core/util';
import { RecetaModal } from '../shared/receta-modal';
import { SolicitudModal } from '../shared/solicitud-modal';

/** Seguimiento de solicitudes (Figs. 75–78) con detalle y motivo de rechazo. */
@Component({
  selector: 'app-paciente-solicitudes',
  imports: [FormsModule, RouterLink, SolicitudModal, RecetaModal],
  template: `
    <div class="pagina-cab row between wrap">
      <div><h1>Solicitudes</h1><p>Sigue el estado de tus pedidos de receta.</p></div>
      <a class="btn" routerLink="/paciente/solicitar">+ Nueva solicitud</a>
    </div>

    <div class="filtros">
      <select class="input" [ngModel]="estado()" (ngModelChange)="estado.set(+$event)" aria-label="Estado" style="max-width:220px;flex:none">
        <option value="0">Todos los estados</option>
        <option value="1">Pendiente</option><option value="2">En revisión</option><option value="3">Emitida</option><option value="4">Rechazada</option>
      </select>
    </div>

    @if (error()) { <div class="aviso error">{{ error() }}</div> }
    @if (cargando()) { <p class="muted">Cargando…</p> }
    @else if (!filtradas().length) {
      <div class="vacio"><b>No hay solicitudes</b>Cuando envíes una solicitud aparecerá aquí con su estado.</div>
    } @else {
      <div class="tabla-wrap"><table class="tabla">
        <thead><tr><th>N°</th><th>Tipo</th><th>Médico</th><th>Fecha</th><th>Estado</th></tr></thead>
        <tbody>
          @for (s of filtradas(); track s.id_solicitud) {
            <tr class="click" (click)="abierta.set(s)">
              <td>#{{ s.id_solicitud }}</td><td>{{ s.tipo_solicitud }}</td><td>{{ s.nombre_medico }}</td><td>{{ f(s.fecha_solicitud) }}</td>
              <td><span class="chip {{ chip(s).clase }}">{{ chip(s).texto }}</span></td>
            </tr>
          }
        </tbody>
      </table></div>
    }

    @if (abierta(); as s) { <app-solicitud-modal [s]="s" (cerrar)="abierta.set(null)" (verReceta)="verReceta($event)" /> }
    @if (receta(); as r) { <app-receta-modal [r]="r" (cerrar)="receta.set(null)" /> }
  `,
})
export class PacienteSolicitudes {
  private api = inject(ApiService);
  solicitudes = signal<Solicitud[]>([]);
  cargando = signal(true);
  error = signal('');
  estado = signal(0);
  abierta = signal<Solicitud | null>(null);
  receta = signal<Receta | null>(null);
  f = fechaCorta; chip = chipSolicitud;
  filtradas = computed(() => this.solicitudes().filter(s => !this.estado() || s.estado_solicitud === this.estado()));

  constructor() {
    this.api.solicitudesDePaciente(inject(AuthService).sesion()!.id_paciente!).subscribe({
      next: s => { this.solicitudes.set(s); this.cargando.set(false); },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  verReceta(id: number) {
    this.api.receta(id).subscribe({ next: r => { this.abierta.set(null); this.receta.set(r); }, error: e => this.error.set(mensajeError(e)) });
  }
}
