import { Component, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, forkJoin, of } from 'rxjs';
import { ApiService } from '../core/api.service';
import { Alergia, Enfermedad, FichaClinica } from '../core/models';
import { mensajeError } from '../core/util';

/**
 * Ficha clínica (Figs. 79 / 100–101). Regla del backend: SOLO el paciente dueño de la ficha puede modificarla;
 * el médico tratante la consulta en solo lectura. Enfermedades y alergias se eligen del catálogo.
 */
@Component({
  selector: 'app-ficha',
  imports: [FormsModule],
  template: `
    @if (cargando()) { <p class="muted">Cargando ficha…</p> }
    @else if (ficha(); as fi) {
      @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
      @if (ok()) { <div class="aviso ok" role="status">{{ ok() }}</div> }

      <section class="card">
        <h3>Enfermedades diagnosticadas</h3>
        <div class="tags mt">
          @for (e of fi.enfermedades; track e) {
            <span class="tag">{{ e }}@if (editable()) { <button type="button" (click)="quitarEnf(e)" [attr.aria-label]="'Quitar ' + e">×</button> }</span>
          } @empty { <span class="muted">Ninguna registrada</span> }
        </div>
        @if (editable()) {
          <div class="row mt">
            <select class="input" name="enf" [(ngModel)]="nuevaEnf" aria-label="Agregar enfermedad">
              <option [ngValue]="null">+ Agregar diagnóstico…</option>
              @for (e of enfDisponibles(); track e.id_enfermedad) { <option [ngValue]="e.id_enfermedad">{{ e.nombre_enfermedad }}</option> }
            </select>
            <button class="btn sec" type="button" [disabled]="!nuevaEnf" (click)="agregarEnf()">Agregar</button>
          </div>
        }
      </section>

      <section class="card">
        <h3>Alergias y reacciones</h3>
        <div class="tags mt">
          @for (a of fi.alergias; track a) {
            <span class="tag" style="background:var(--rojo-bg);color:var(--rojo)">{{ a }}@if (editable()) { <button type="button" (click)="quitarAle(a)" [attr.aria-label]="'Quitar ' + a">×</button> }</span>
          } @empty { <span class="muted">Ninguna registrada</span> }
        </div>
        @if (editable()) {
          <div class="row mt">
            <select class="input" name="ale" [(ngModel)]="nuevaAle" aria-label="Registrar alergia">
              <option [ngValue]="null">+ Registrar nueva alergia…</option>
              @for (a of aleDisponibles(); track a.id_alergia) { <option [ngValue]="a.id_alergia">{{ a.nombre_alergia }}</option> }
            </select>
            <button class="btn sec" type="button" [disabled]="!nuevaAle" (click)="agregarAle()">Agregar</button>
          </div>
        }
      </section>

      <section class="card">
        <h3>Datos de seguridad y antropometría</h3>
        @if (editable()) {
          <div class="fila-3">
            <div><label class="lbl">Grupo sanguíneo</label>
              <select class="input" name="gs" [(ngModel)]="tipo">
                <option value="">Sin registrar</option>
                @for (g of grupos; track g) { <option>{{ g }}</option> }
              </select></div>
            <div><label class="lbl">Peso (kg)</label><input class="input" name="peso" type="number" min="1" step="0.1" [(ngModel)]="peso"></div>
            <div><label class="lbl">Talla (cm)</label><input class="input" name="talla" type="number" min="30" step="0.1" [(ngModel)]="talla"></div>
          </div>
          <div class="row mt"><button class="btn" type="button" [disabled]="guardando()" (click)="guardar()">{{ guardando() ? 'Guardando…' : 'Guardar cambios' }}</button></div>
        } @else {
          <div class="grid-3 mt">
            <div class="dato"><span>Grupo sanguíneo</span><b>{{ fi.tipo_sangre || 'No registrado' }}</b></div>
            <div class="dato"><span>Peso</span><b>{{ fi.peso ? fi.peso + ' kg' : 'No registrado' }}</b></div>
            <div class="dato"><span>Talla</span><b>{{ fi.talla ? fi.talla + ' cm' : 'No registrado' }}</b></div>
          </div>
          <p class="muted small mt">La ficha clínica solo la actualiza el propio paciente; tú puedes consultarla.</p>
        }
      </section>
    } @else if (error()) { <div class="aviso error">{{ error() }}</div> }
  `,
})
export class FichaClinicaView {
  private api = inject(ApiService);
  idPaciente = input.required<number>();
  editable = input(false);

  ficha = signal<FichaClinica | null>(null);
  enfermedades = signal<Enfermedad[]>([]);
  alergias = signal<Alergia[]>([]);
  cargando = signal(true);
  guardando = signal(false);
  error = signal('');
  ok = signal('');
  grupos = ['O+', 'O-', 'A+', 'A-', 'B+', 'B-', 'AB+', 'AB-'];
  tipo = ''; peso: number | null = null; talla: number | null = null;
  nuevaEnf: number | null = null; nuevaAle: number | null = null;

  enfDisponibles = () => this.enfermedades().filter(e => e.estado_enfermedad !== 0 && !this.ficha()?.enfermedades.includes(e.nombre_enfermedad));
  aleDisponibles = () => this.alergias().filter(a => a.estado_alergia !== 0 && !this.ficha()?.alergias.includes(a.nombre_alergia));

  ngOnInit() {
    forkJoin({
      fi: this.api.ficha(this.idPaciente()),
      en: this.editable() ? this.api.enfermedades() : of([] as Enfermedad[]),
      al: this.editable() ? this.api.alergias() : of([] as Alergia[]),
    }).subscribe({
      next: ({ fi, en, al }) => { this.enfermedades.set(en); this.alergias.set(al); this.aplicar(fi); this.cargando.set(false); },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }

  private aplicar(fi: FichaClinica) {
    this.ficha.set(fi);
    this.tipo = fi.tipo_sangre ?? ''; this.peso = fi.peso; this.talla = fi.talla;
  }
  private accion(o: Observable<FichaClinica>, msg: string) {
    this.error.set(''); this.ok.set('');
    o.subscribe({
      next: fi => { const t = this.tipo, p = this.peso, ta = this.talla; this.aplicar(fi); this.tipo = t; this.peso = p; this.talla = ta; this.ok.set(msg); this.guardando.set(false); },
      error: e => { this.error.set(mensajeError(e)); this.guardando.set(false); },
    });
  }

  agregarEnf() { const id = this.nuevaEnf!; this.nuevaEnf = null; this.accion(this.api.agregarEnfermedad(this.idPaciente(), id), 'Diagnóstico agregado.'); }
  agregarAle() { const id = this.nuevaAle!; this.nuevaAle = null; this.accion(this.api.agregarAlergia(this.idPaciente(), id), 'Alergia registrada.'); }
  quitarEnf(nombre: string) {
    const e = this.enfermedades().find(x => x.nombre_enfermedad === nombre); if (!e) return;
    this.accion(this.api.quitarEnfermedad(this.idPaciente(), e.id_enfermedad), 'Diagnóstico quitado.');
  }
  quitarAle(nombre: string) {
    const a = this.alergias().find(x => x.nombre_alergia === nombre); if (!a) return;
    this.accion(this.api.quitarAlergia(this.idPaciente(), a.id_alergia), 'Alergia quitada.');
  }
  guardar() {
    this.guardando.set(true);
    this.accion(this.api.guardarDatosClinicos(this.idPaciente(), { tipo_sangre: this.tipo, peso: this.peso, talla: this.talla }), 'Datos guardados correctamente.');
  }
}
