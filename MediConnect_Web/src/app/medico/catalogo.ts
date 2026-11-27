import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, forkJoin } from 'rxjs';
import { ApiService } from '../core/api.service';
import { Alergia, Enfermedad, Medicamento } from '../core/models';
import { mensajeError } from '../core/util';

type Pestana = 'medicamentos' | 'enfermedades' | 'alergias';

/**
 * Catálogo (agregado al diseño del informe): permite cargar desde la web los medicamentos, enfermedades y alergias
 * que luego eligen el paciente (solicitudes, ficha) y el médico (recetas). La baja es lógica (queda en auditoría).
 */
@Component({
  selector: 'app-medico-catalogo',
  imports: [FormsModule],
  template: `
    <div class="pagina-cab"><h1>Catálogo</h1><p>Medicamentos, enfermedades y alergias disponibles en MediConnect.</p></div>

    <div class="row wrap" style="margin-bottom:18px">
      <button class="btn peq" [class.sec]="tab() !== 'medicamentos'" (click)="cambiar('medicamentos')">Medicamentos ({{ medicamentos().length }})</button>
      <button class="btn peq" [class.sec]="tab() !== 'enfermedades'" (click)="cambiar('enfermedades')">Enfermedades ({{ enfermedades().length }})</button>
      <button class="btn peq" [class.sec]="tab() !== 'alergias'" (click)="cambiar('alergias')">Alergias ({{ alergias().length }})</button>
    </div>

    @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
    @if (ok()) { <div class="aviso ok" role="status">{{ ok() }}</div> }

    <form class="card" (ngSubmit)="agregar()" novalidate>
      <h3>Agregar {{ tab() === 'medicamentos' ? 'medicamento' : tab() === 'enfermedades' ? 'enfermedad' : 'alergia' }}</h3>
      <div class="fila mt">
        <div>
          <label class="lbl" for="nom">Nombre</label>
          <input id="nom" name="nom" class="input" [(ngModel)]="nombre" maxlength="100"
                 [placeholder]="tab() === 'medicamentos' ? 'Ej.: Losartán' : tab() === 'enfermedades' ? 'Ej.: Hipertensión arterial' : 'Ej.: Penicilina'">
        </div>
        @if (tab() === 'medicamentos') {
          <div>
            <label class="lbl" for="con">Concentración</label>
            <input id="con" name="con" class="input" [(ngModel)]="concentracion" maxlength="50" placeholder="Ej.: 50 mg">
          </div>
        }
      </div>
      <div class="row mt"><button class="btn" type="submit" [disabled]="guardando()">{{ guardando() ? 'Guardando…' : '+ Agregar' }}</button></div>
    </form>

    @if (cargando()) { <p class="muted mt">Cargando…</p> }
    @else {
      <div class="tabla-wrap mt"><table class="tabla">
        <thead><tr><th>Nombre</th>@if (tab() === 'medicamentos') { <th>Concentración</th> }<th style="width:120px"></th></tr></thead>
        <tbody>
          @switch (tab()) {
            @case ('medicamentos') {
              @for (m of medicamentos(); track m.id_medicamento) {
                <tr><td><b>{{ m.nombre_medicamento }}</b></td><td>{{ m.concentracion }}</td>
                  <td><button class="btn peligro peq" (click)="quitar(m.id_medicamento)">Eliminar</button></td></tr>
              } @empty { <tr><td colspan="3" class="muted">Aún no hay medicamentos. Agrega el primero arriba.</td></tr> }
            }
            @case ('enfermedades') {
              @for (e of enfermedades(); track e.id_enfermedad) {
                <tr><td><b>{{ e.nombre_enfermedad }}</b></td>
                  <td><button class="btn peligro peq" (click)="quitar(e.id_enfermedad)">Eliminar</button></td></tr>
              } @empty { <tr><td colspan="2" class="muted">Aún no hay enfermedades. Agrega la primera arriba.</td></tr> }
            }
            @case ('alergias') {
              @for (a of alergias(); track a.id_alergia) {
                <tr><td><b>{{ a.nombre_alergia }}</b></td>
                  <td><button class="btn peligro peq" (click)="quitar(a.id_alergia)">Eliminar</button></td></tr>
              } @empty { <tr><td colspan="2" class="muted">Aún no hay alergias. Agrega la primera arriba.</td></tr> }
            }
          }
        </tbody>
      </table></div>
    }
  `,
})
export class MedicoCatalogo {
  private api = inject(ApiService);
  tab = signal<Pestana>('medicamentos');
  medicamentos = signal<Medicamento[]>([]);
  enfermedades = signal<Enfermedad[]>([]);
  alergias = signal<Alergia[]>([]);
  cargando = signal(true);
  guardando = signal(false);
  error = signal('');
  ok = signal('');
  nombre = ''; concentracion = '';

  constructor() { this.cargar(); }

  private cargar() {
    forkJoin({ m: this.api.medicamentos(), e: this.api.enfermedades(), a: this.api.alergias() }).subscribe({
      next: ({ m, e, a }) => { this.medicamentos.set(m); this.enfermedades.set(e); this.alergias.set(a); this.cargando.set(false); },
      error: x => { this.error.set(mensajeError(x)); this.cargando.set(false); },
    });
  }

  cambiar(t: Pestana) { this.tab.set(t); this.nombre = ''; this.concentracion = ''; this.error.set(''); this.ok.set(''); }

  agregar() {
    this.error.set(''); this.ok.set('');
    const n = this.nombre.trim(), c = this.concentracion.trim();
    if (!n) { this.error.set('Escribe el nombre.'); return; }
    if (this.tab() === 'medicamentos' && !c) { this.error.set('Escribe la concentración (ej.: 50 mg).'); return; }
    this.guardando.set(true);
    const llamada: Observable<unknown> = this.tab() === 'medicamentos' ? this.api.guardarMedicamento(n, c)
      : this.tab() === 'enfermedades' ? this.api.guardarEnfermedad(n) : this.api.guardarAlergia(n);
    llamada.subscribe({
      next: () => { this.guardando.set(false); this.nombre = ''; this.concentracion = ''; this.ok.set('Agregado correctamente.'); this.cargar(); },
      error: x => { this.guardando.set(false); this.error.set(mensajeError(x)); },
    });
  }

  quitar(id: number) {
    this.error.set(''); this.ok.set('');
    const llamada = this.tab() === 'medicamentos' ? this.api.eliminarMedicamento(id)
      : this.tab() === 'enfermedades' ? this.api.eliminarEnfermedad(id) : this.api.eliminarAlergia(id);
    llamada.subscribe({
      next: () => { this.ok.set('Eliminado correctamente.'); this.cargar(); },
      error: x => this.error.set(mensajeError(x)),
    });
  }
}
