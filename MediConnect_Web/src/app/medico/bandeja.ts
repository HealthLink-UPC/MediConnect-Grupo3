import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Solicitud } from '../core/models';
import { chipSolicitud, fechaCorta, mensajeError } from '../core/util';

/** Bandeja del médico (Figs. 91–92): solicitudes con filtros por paciente, fecha y estado. */
@Component({
  selector: 'app-medico-bandeja',
  imports: [FormsModule],
  template: `
    <div class="pagina-cab"><div class="eyebrow">Portal médico</div><h1>Bandeja de solicitudes</h1>
      <p>Revisa las solicitudes de renovación y nuevas recetas de tus pacientes.</p></div>

    <div class="grid-4">
      <div class="card kpi"><span class="muted">Pendientes</span><b>{{ cuenta(1) }}</b></div>
      <div class="card kpi"><span class="muted">En revisión</span><b>{{ cuenta(2) }}</b></div>
      <div class="card kpi"><span class="muted">Emitidas</span><b>{{ cuenta(3) }}</b></div>
      <div class="card kpi"><span class="muted">Rechazadas</span><b>{{ cuenta(4) }}</b></div>
    </div>

    <div class="filtros">
      <input class="input" placeholder="Buscar paciente…" [ngModel]="texto()" (ngModelChange)="texto.set($event)" aria-label="Buscar paciente">
      <input class="input" type="date" [ngModel]="fecha()" (ngModelChange)="fecha.set($event)" aria-label="Fecha" style="max-width:190px;flex:none">
      <select class="input" [ngModel]="estado()" (ngModelChange)="estado.set(+$event)" aria-label="Estado" style="max-width:200px;flex:none">
        <option value="0">Todos los estados</option><option value="1">Pendiente</option><option value="2">En revisión</option><option value="3">Emitida</option><option value="4">Rechazada</option>
      </select>
      @if (texto() || fecha() || estado()) { <button class="btn sec peq" (click)="limpiar()">Limpiar</button> }
    </div>

    @if (error()) { <div class="aviso error">{{ error() }}</div> }
    @if (cargando()) { <p class="muted">Cargando…</p> }
    @else if (!filtradas().length) {
      <div class="vacio"><b>{{ todas().length ? 'Sin resultados' : 'No tienes solicitudes' }}</b>
        {{ todas().length ? 'Prueba con otros filtros.' : 'Cuando un paciente te envíe una solicitud aparecerá aquí.' }}</div>
    } @else {
      <div class="tabla-wrap"><table class="tabla">
        <thead><tr><th>N°</th><th>Paciente</th><th>Tipo</th><th>Medicamentos</th><th>Fecha</th><th>Estado</th></tr></thead>
        <tbody>
          @for (s of filtradas(); track s.id_solicitud) {
            <tr class="click" (click)="abrir(s)">
              <td>#{{ s.id_solicitud }}</td>
              <td><b>{{ s.nombre_paciente }}</b></td>
              <td>{{ s.tipo_solicitud }}</td>
              <td class="muted">{{ meds(s) }}</td>
              <td>{{ f(s.fecha_solicitud) }}</td>
              <td><span class="chip {{ chip(s).clase }}">{{ chip(s).texto }}</span></td>
            </tr>
          }
        </tbody>
      </table></div>
    }
  `,
})
export class MedicoBandeja {
  private api = inject(ApiService);
  private router = inject(Router);
  todas = signal<Solicitud[]>([]);
  cargando = signal(true);
  error = signal('');
  texto = signal(''); fecha = signal(''); estado = signal(0);
  f = fechaCorta; chip = chipSolicitud;

  filtradas = computed(() => {
    const t = this.texto().trim().toLowerCase();
    return this.todas().filter(s =>
      (!t || s.nombre_paciente.toLowerCase().includes(t)) &&
      (!this.fecha() || s.fecha_solicitud === this.fecha()) &&
      (!this.estado() || s.estado_solicitud === this.estado()));
  });
  cuenta = (e: number) => this.todas().filter(s => s.estado_solicitud === e).length;
  meds = (s: Solicitud) => s.medicamentos.map(m => `${m.nombre_medicamento} ${m.concentracion ?? ''}`.trim()).join(', ') || '—';

  constructor() {
    this.api.solicitudesDeMedico(inject(AuthService).sesion()!.id_medico!).subscribe({
      next: s => { this.todas.set(s); this.cargando.set(false); },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  limpiar() { this.texto.set(''); this.fecha.set(''); this.estado.set(0); }
  abrir(s: Solicitud) { this.router.navigate(['/medico/revision', s.id_solicitud]); }
}
