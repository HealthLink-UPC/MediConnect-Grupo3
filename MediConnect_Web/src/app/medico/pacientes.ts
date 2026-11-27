import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Solicitud } from '../core/models';
import { fechaCorta, iniciales, mensajeError } from '../core/util';

interface PacienteResumen { id: number; nombre: string; solicitudes: number; ultima: string; }

/**
 * Pacientes (Fig. 99–100). El backend no tiene un listado de pacientes por médico:
 * se arma con los pacientes que le han enviado solicitudes.
 */
@Component({
  selector: 'app-medico-pacientes',
  imports: [FormsModule],
  template: `
    <div class="pagina-cab"><h1>Pacientes</h1><p>Pacientes que te han enviado solicitudes.</p></div>
    <div class="filtros"><input class="input" placeholder="Buscar paciente…" [ngModel]="texto()" (ngModelChange)="texto.set($event)" aria-label="Buscar paciente"></div>
    @if (error()) { <div class="aviso error">{{ error() }}</div> }
    @if (cargando()) { <p class="muted">Cargando…</p> }
    @else if (!filtrados().length) { <div class="vacio"><b>Sin pacientes</b>Aún no tienes pacientes con solicitudes.</div> }
    @else {
      <div class="tabla-wrap"><table class="tabla">
        <thead><tr><th>Paciente</th><th>Solicitudes</th><th>Última solicitud</th><th></th></tr></thead>
        <tbody>
          @for (p of filtrados(); track p.id) {
            <tr class="click" (click)="abrir(p.id)">
              <td><div class="row"><span class="avatar claro">{{ ini(p.nombre) }}</span><b>{{ p.nombre }}</b></div></td>
              <td>{{ p.solicitudes }}</td><td>{{ f(p.ultima) }}</td>
              <td style="text-align:right"><span class="btn sec peq">Ver ficha</span></td>
            </tr>
          }
        </tbody>
      </table></div>
    }
  `,
})
export class MedicoPacientes {
  private router = inject(Router);
  pacientes = signal<PacienteResumen[]>([]);
  cargando = signal(true);
  error = signal('');
  texto = signal('');
  f = fechaCorta; ini = iniciales;
  filtrados = computed(() => {
    const t = this.texto().trim().toLowerCase();
    return this.pacientes().filter(p => !t || p.nombre.toLowerCase().includes(t));
  });

  constructor() {
    inject(ApiService).solicitudesDeMedico(inject(AuthService).sesion()!.id_medico!).subscribe({
      next: (lista: Solicitud[]) => {
        const m = new Map<number, PacienteResumen>();
        for (const s of lista) {
          const p = m.get(s.id_paciente) ?? { id: s.id_paciente, nombre: s.nombre_paciente, solicitudes: 0, ultima: s.fecha_solicitud };
          p.solicitudes++; if (s.fecha_solicitud > p.ultima) p.ultima = s.fecha_solicitud;
          m.set(s.id_paciente, p);
        }
        this.pacientes.set([...m.values()].sort((a, b) => a.nombre.localeCompare(b.nombre)));
        this.cargando.set(false);
      },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }
  abrir(id: number) { this.router.navigate(['/medico/pacientes', id]); }
}
