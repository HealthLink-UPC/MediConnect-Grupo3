import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Receta } from '../core/models';
import { chipReceta, fechaCorta, mensajeError, nombreDeMedicamentos } from '../core/util';
import { RecetaModal } from '../shared/receta-modal';

/** Mis recetas (Figs. 72–74): filtros por medicamento, fecha y estado + detalle. */
@Component({
  selector: 'app-paciente-recetas',
  imports: [FormsModule, RecetaModal],
  template: `
    <div class="pagina-cab"><h1>Mis recetas</h1><p>Consulta tus recetas digitales y solicita su renovación.</p></div>

    <div class="filtros">
      <input class="input" placeholder="Buscar medicamento…" [ngModel]="texto()" (ngModelChange)="texto.set($event)" aria-label="Buscar medicamento">
      <input class="input" type="date" [ngModel]="fecha()" (ngModelChange)="fecha.set($event)" aria-label="Fecha de emisión" style="max-width:190px;flex:none">
      <select class="input" [ngModel]="estado()" (ngModelChange)="estado.set($event)" aria-label="Estado" style="max-width:190px;flex:none">
        <option value="">Todos los estados</option><option>Vigente</option><option>Por vencer</option><option>Vencida</option>
      </select>
      @if (texto() || fecha() || estado()) { <button class="btn sec peq" (click)="limpiar()">Limpiar</button> }
    </div>

    @if (error()) { <div class="aviso error">{{ error() }}</div> }
    @if (cargando()) { <p class="muted">Cargando…</p> }
    @else if (!filtradas().length) {
      <div class="vacio"><b>{{ recetas().length ? 'Sin resultados' : 'Aún no tienes recetas' }}</b>
        {{ recetas().length ? 'Prueba con otros filtros.' : 'Cuando tu médico apruebe una solicitud, tu receta aparecerá aquí.' }}</div>
    } @else {
      <div class="tabla-wrap"><table class="tabla">
        <thead><tr><th>Receta</th><th>Medicamentos</th><th>Médico</th><th>Emisión</th><th>Vence</th><th>Estado</th></tr></thead>
        <tbody>
          @for (r of filtradas(); track r.id_receta) {
            <tr class="click" (click)="abierta.set(r)">
              <td>#{{ r.id_receta }}</td><td>{{ nombres(r) }}</td><td>{{ r.nombre_medico }}</td>
              <td>{{ f(r.fecha_emision) }}</td><td>{{ f(r.fecha_vencimiento) }}</td>
              <td><span class="chip {{ chip(r).clase }}">{{ chip(r).texto }}</span></td>
            </tr>
          }
        </tbody>
      </table></div>
    }

    @if (abierta(); as r) {
      <app-receta-modal [r]="r" [puedeRenovar]="true" (cerrar)="abierta.set(null)" (renovar)="renovar(r)" />
    }
  `,
})
export class PacienteRecetas {
  private api = inject(ApiService);
  private router = inject(Router);
  recetas = signal<Receta[]>([]);
  cargando = signal(true);
  error = signal('');
  abierta = signal<Receta | null>(null);
  texto = signal(''); fecha = signal(''); estado = signal('');
  f = fechaCorta; chip = chipReceta; nombres = nombreDeMedicamentos;

  filtradas = computed(() => {
    const t = this.texto().trim().toLowerCase();
    return this.recetas().filter(r =>
      (!t || nombreDeMedicamentos(r).toLowerCase().includes(t)) &&
      (!this.fecha() || r.fecha_emision === this.fecha()) &&
      (!this.estado() || chipReceta(r).texto === this.estado()));
  });

  constructor() {
    this.api.recetasDePaciente(inject(AuthService).sesion()!.id_paciente!).subscribe({
      next: r => { this.recetas.set(r); this.cargando.set(false); },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  limpiar() { this.texto.set(''); this.fecha.set(''); this.estado.set(''); }
  renovar(r: Receta) { this.router.navigate(['/paciente/solicitar'], { queryParams: { receta: r.id_receta } }); }
}
