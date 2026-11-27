import { Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Medicamento, Medico, Receta, Solicitud } from '../core/models';
import { chipReceta, fechaCorta, mensajeError, nombreDeMedicamentos } from '../core/util';

/**
 * Solicitar receta (Figs. 75, 80–86): renovación de una receta existente o nueva receta (con médico y medicamentos).
 * Si no hay conexión (status 0) se muestra el modal «Error de conexión» con Reintentar envío (Fig. 85).
 */
@Component({
  selector: 'app-paciente-solicitar',
  imports: [FormsModule, RouterLink],
  template: `
    <div class="pagina-cab"><h1>Solicitar receta</h1><p>Elige si quieres renovar una receta o pedir una nueva.</p></div>

    <div class="row wrap" style="margin-bottom:18px">
      <button class="btn peq" [class.sec]="modo() !== 'renovar'" (click)="modo.set('renovar')">Renovar una receta</button>
      <button class="btn peq" [class.sec]="modo() !== 'nueva'" (click)="modo.set('nueva')">Nueva receta</button>
    </div>

    @if (cargando()) { <p class="muted">Cargando…</p> } @else {
      <form class="card" (ngSubmit)="enviar()" novalidate style="max-width:760px">
        @if (modo() === 'renovar') {
          <h3>Renovación de receta</h3>
          @if (!recetas().length) {
            <div class="aviso info">Aún no tienes recetas emitidas para renovar. Usa «Nueva receta».</div>
          } @else {
            <label class="lbl" for="rec">Receta a renovar</label>
            <select id="rec" name="rec" class="input" [(ngModel)]="idReceta">
              <option [ngValue]="null">Selecciona una receta…</option>
              @for (r of recetas(); track r.id_receta) {
                <option [ngValue]="r.id_receta">#{{ r.id_receta }} · {{ nombres(r) }} ({{ chip(r).texto }}, vence {{ f(r.fecha_vencimiento) }})</option>
              }
            </select>
          }
        } @else {
          <h3>Nueva receta</h3>
          <label class="lbl" for="med">Médico</label>
          <select id="med" name="med" class="input" [(ngModel)]="idMedico">
            <option [ngValue]="null">Selecciona un médico…</option>
            @for (m of medicos(); track m.id_medico) { <option [ngValue]="m.id_medico">{{ m.nombre_medico }} — {{ m.especialidad }} ({{ m.centro_atencion }})</option> }
          </select>
          <label class="lbl">Medicamentos que necesitas</label>
          <div class="busca-lista">
            @for (m of catalogo(); track m.id_medicamento) {
              <label><input type="checkbox" [checked]="elegidos().has(m.id_medicamento)" (change)="alternar(m.id_medicamento)">
                {{ m.nombre_medicamento }} <span class="muted">{{ m.concentracion }}</span></label>
            } @empty { <div style="padding:14px" class="muted">El catálogo de medicamentos está vacío.</div> }
          </div>
        }

        <label class="lbl" for="mot">Comentario para tu médico</label>
        <textarea id="mot" name="mot" class="input" [(ngModel)]="motivo" placeholder="Ej.: Me quedan pocos comprimidos. Presión estable en controles domiciliarios."></textarea>
        <p class="muted small" style="margin-top:6px">Puedes indicar la cantidad que necesitas y cómo te has sentido.</p>

        @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
        <div class="row mt">
          <a class="btn sec" routerLink="/paciente/solicitudes">Cancelar</a>
          <button class="btn" type="submit" [disabled]="enviando()">{{ enviando() ? 'Enviando…' : 'Enviar solicitud' }}</button>
        </div>
      </form>
    }

    @if (enviada(); as s) {
      <div class="fondo-modal"><div class="modal" role="dialog" aria-modal="true">
        <h2>Solicitud enviada</h2>
        <p class="muted" style="text-align:center">Tu solicitud #{{ s.id_solicitud }} fue enviada a {{ s.nombre_medico }}.</p>
        <dl class="resumen">
          <div><dt>Tipo</dt><b>{{ s.tipo_solicitud }}</b></div>
          <div><dt>Estado</dt><b>{{ s.estado_nombre }}</b></div>
          <div><dt>Médico</dt><b>{{ s.nombre_medico }}</b></div>
        </dl>
        <div class="acciones"><button class="btn" (click)="irASolicitudes()">Ver mis solicitudes</button></div>
      </div></div>
    }

    @if (sinConexion()) {
      <div class="fondo-modal"><div class="modal" role="alertdialog" aria-modal="true">
        <h2>Error de conexión</h2>
        <p class="muted">Detectamos una falla en tu conexión a internet. Tu solicitud no pudo ser entregada.</p>
        <dl class="resumen">
          <div><dt>Estado</dt><b>No enviado (sin conexión)</b></div>
          <div><dt>Datos</dt><b>Conservados en esta pantalla</b></div>
        </dl>
        <p class="muted small">Verifica tu red wifi o datos móviles para reintentar.</p>
        <div class="acciones">
          <button class="btn" (click)="enviar()">Reintentar envío</button>
          <button class="btn sec" (click)="sinConexion.set(false)">→ Volver</button>
        </div>
      </div></div>
    }
  `,
})
export class PacienteSolicitar {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);

  /** ?receta=ID (viene del detalle de receta) deja la renovación preseleccionada. */
  receta = input<string>();

  modo = signal<'renovar' | 'nueva'>('renovar');
  recetas = signal<Receta[]>([]);
  medicos = signal<Medico[]>([]);
  catalogo = signal<Medicamento[]>([]);
  elegidos = signal<Set<number>>(new Set());
  cargando = signal(true);
  enviando = signal(false);
  error = signal('');
  sinConexion = signal(false);
  enviada = signal<Solicitud | null>(null);
  idReceta: number | null = null;
  idMedico: number | null = null;
  motivo = '';
  f = fechaCorta; chip = chipReceta; nombres = nombreDeMedicamentos;
  vacias = computed(() => this.recetas().length === 0);

  constructor() {
    forkJoin({
      r: this.api.recetasDePaciente(this.auth.sesion()!.id_paciente!),
      m: this.api.medicos(),
      c: this.api.medicamentos(),
    }).subscribe({
      next: ({ r, m, c }) => {
        this.recetas.set(r); this.medicos.set(m); this.catalogo.set(c.filter(x => x.estado_medicamento !== 0));
        const pre = Number(this.receta());
        if (pre && r.some(x => x.id_receta === pre)) this.idReceta = pre;
        else if (!r.length) this.modo.set('nueva');
        this.cargando.set(false);
      },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }

  alternar(id: number) {
    const s = new Set(this.elegidos());
    s.has(id) ? s.delete(id) : s.add(id);
    this.elegidos.set(s);
  }

  enviar() {
    this.error.set(''); this.sinConexion.set(false);
    let llamada;
    if (this.modo() === 'renovar') {
      if (!this.idReceta) { this.error.set('Selecciona la receta que quieres renovar.'); return; }
      llamada = this.api.solicitarRenovacion(this.idReceta, this.motivo.trim());
    } else {
      if (!this.idMedico) { this.error.set('Selecciona un médico.'); return; }
      if (!this.elegidos().size) { this.error.set('Selecciona al menos un medicamento.'); return; }
      llamada = this.api.solicitarNuevaReceta(this.idMedico, this.motivo.trim(), [...this.elegidos()]);
    }
    this.enviando.set(true);
    llamada.subscribe({
      next: s => { this.enviando.set(false); this.enviada.set(s); },
      error: e => {
        this.enviando.set(false);
        if (e.status === 0) this.sinConexion.set(true); else this.error.set(mensajeError(e));
      },
    });
  }
  irASolicitudes() { this.router.navigate(['/paciente/solicitudes']); }
}
