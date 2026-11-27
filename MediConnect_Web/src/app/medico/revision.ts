import { Component, computed, inject, input, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { FichaClinica, Receta, Solicitud } from '../core/models';
import { aISO, chipSolicitud, fechaCorta, hoyISO, iniciales, mensajeError } from '../core/util';

interface Linea { id_medicamento: number; nombre: string; dosis: string; frecuencia: string; fecha_inicio: string; fecha_fin: string; }

const enDias = (n: number) => { const d = new Date(); d.setDate(d.getDate() + n); return aISO(d); };

/**
 * Revisión de renovación (Figs. 93–97): datos del paciente, ficha, tratamientos solicitados y decisión médica.
 * Al abrir una solicitud «Pendiente» pasa automáticamente a «En revisión».
 */
@Component({
  selector: 'app-medico-revision',
  imports: [FormsModule, RouterLink],
  template: `
    <a routerLink="/medico/bandeja" class="small">← Volver a la bandeja</a>
    @if (cargando()) { <p class="muted mt">Cargando…</p> }
    @else if (error() && !s()) { <div class="aviso error mt">{{ error() }}</div> }
    @else if (s(); as sol) {
      <div class="pagina-cab mt">
        <div class="eyebrow">Solicitud #{{ sol.id_solicitud }} · {{ sol.tipo_solicitud }}</div>
        <div class="row wrap"><h1>Revisión de solicitud</h1><span class="chip {{ chip(sol).clase }}">{{ chip(sol).texto }}</span></div>
        <p>Recibida el {{ f(sol.fecha_solicitud) }}</p>
      </div>

      <section class="card">
        <div class="row between wrap">
          <div class="row"><span class="avatar claro" style="width:52px;height:52px">{{ ini(sol.nombre_paciente) }}</span>
            <div><h2>{{ sol.nombre_paciente }}</h2></div></div>
          <a class="btn sec peq" [routerLink]="['/medico/pacientes', sol.id_paciente]">Ver ficha clínica</a>
        </div>
        @if (ficha(); as fi) {
          <div class="grid-3 mt">
            <div class="dato"><span>Enfermedades</span><b>{{ fi.enfermedades.join(', ') || 'Ninguna registrada' }}</b></div>
            <div class="dato"><span>Alergias</span><b>{{ fi.alergias.join(', ') || 'Ninguna registrada' }}</b></div>
            <div class="dato"><span>Grupo sanguíneo</span><b>{{ fi.tipo_sangre || 'No registrado' }}</b></div>
          </div>
        }
      </section>

      <h2 class="mt-lg" style="margin-bottom:14px">Tratamientos solicitados ({{ lineas().length || sol.medicamentos.length }})</h2>
      <div class="grid-2" style="grid-template-columns: 1fr 340px; align-items:start">
        <div>
          @for (l of lineas(); track l.id_medicamento) {
            <section class="card">
              <div class="row"><span class="icono-pastilla">💊</span><h3>{{ l.nombre }}</h3></div>
              <div class="fila mt">
                <div><label class="lbl">Dosis</label><input class="input" [name]="'d' + l.id_medicamento" [(ngModel)]="l.dosis" [disabled]="!editable()" placeholder="1 comprimido"></div>
                <div><label class="lbl">Frecuencia</label><input class="input" [name]="'f' + l.id_medicamento" [(ngModel)]="l.frecuencia" [disabled]="!editable()" placeholder="cada 24 horas"></div>
              </div>
              <div class="fila">
                <div><label class="lbl">Inicio</label><input class="input" type="date" [(ngModel)]="l.fecha_inicio" [name]="'i' + l.id_medicamento" [disabled]="!editable()"></div>
                <div><label class="lbl">Fin de la receta</label><input class="input" type="date" [(ngModel)]="l.fecha_fin" [name]="'e' + l.id_medicamento" [disabled]="!editable()"></div>
              </div>
            </section>
          } @empty {
            <div class="vacio">Esta solicitud no incluye medicamentos.</div>
          }
          @if (sol.motivo) {
            <section class="card"><div class="muted small">Comentario del paciente</div><p style="margin-top:6px">{{ sol.motivo }}</p></section>
          }
        </div>

        <aside class="card" style="margin-top:0">
          <h3>Decisión médica</h3>
          @if (editable()) {
            <p class="muted mt">Confirma que revisaste los antecedentes y la indicación antes de emitir.</p>
            <label class="check mt"><input type="checkbox" [(ngModel)]="revisado" name="rev"> He revisado la ficha clínica y la solicitud.</label>
            @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
            <button class="btn block mt" [disabled]="!revisado || procesando()" (click)="confirmar.set(true)">Aprobar y emitir</button>
            <button class="btn sec block" style="margin-top:10px" [disabled]="procesando()" (click)="rechazando.set(true)">Rechazar solicitud</button>
            <p class="muted small mt">Toda decisión queda registrada con fecha, hora y profesional responsable.</p>
          } @else {
            <p class="muted mt">Esta solicitud ya fue resuelta.</p>
            @if (sol.estado_solicitud === 4 && sol.motivo_estado) { <div class="aviso error"><b>Motivo del rechazo:</b> {{ sol.motivo_estado }}</div> }
          }
        </aside>
      </div>
    }

    @if (confirmar()) {
      <div class="fondo-modal"><div class="modal" role="alertdialog" aria-modal="true">
        <h2>Confirmar emisión</h2>
        <p class="muted" style="text-align:center">Se emitirá la receta digital para {{ s()?.nombre_paciente }} con los medicamentos indicados.</p>
        <div class="acciones">
          <button class="btn" [disabled]="procesando()" (click)="aprobar()">{{ procesando() ? 'Emitiendo…' : 'Confirmar y emitir' }}</button>
          <button class="btn sec" (click)="confirmar.set(false)">Cancelar</button>
        </div>
      </div></div>
    }

    @if (rechazando()) {
      <div class="fondo-modal"><div class="modal" role="dialog" aria-modal="true">
        <h2>Rechazar solicitud</h2>
        <p class="muted" style="text-align:center">Indica el motivo; el paciente lo verá en su solicitud.</p>
        <label class="lbl" for="mr">Motivo del rechazo</label>
        <textarea id="mr" name="mr" class="input" [(ngModel)]="motivoRechazo" placeholder="Ej.: Se requiere control presencial antes de renovar."></textarea>
        @if (errorRechazo()) { <div class="campo-error">{{ errorRechazo() }}</div> }
        <div class="acciones">
          <button class="btn peligro" [disabled]="procesando()" (click)="rechazar()">{{ procesando() ? 'Rechazando…' : 'Rechazar' }}</button>
          <button class="btn sec" (click)="rechazando.set(false)">Cancelar</button>
        </div>
      </div></div>
    }

    @if (emitida(); as r) {
      <div class="fondo-modal"><div class="modal" role="dialog" aria-modal="true">
        <h2>Receta emitida</h2>
        <div class="exito" style="margin:14px 0"><div class="tic">✓</div>
          <p>La receta <b>#{{ r.id_receta }}</b> fue emitida correctamente.</p></div>
        <dl class="resumen">
          <div><dt>Paciente</dt><b>{{ r.nombre_paciente }}</b></div>
          <div><dt>Vence</dt><b>{{ f(r.fecha_vencimiento) }}</b></div>
          <div><dt>Código</dt><b style="font-family:monospace">{{ r.codigo_verificacion }}</b></div>
        </dl>
        <div class="acciones"><button class="btn" (click)="volver()">Volver a la bandeja</button></div>
      </div></div>
    }
  `,
})
export class MedicoRevision {
  private api = inject(ApiService);
  private router = inject(Router);
  private auth = inject(AuthService);

  id = input.required<string>();            // :id de la ruta
  s = signal<Solicitud | null>(null);
  ficha = signal<FichaClinica | null>(null);
  lineas = signal<Linea[]>([]);
  cargando = signal(true);
  procesando = signal(false);
  confirmar = signal(false);
  rechazando = signal(false);
  emitida = signal<Receta | null>(null);
  error = signal('');
  errorRechazo = signal('');
  revisado = false;
  motivoRechazo = '';
  f = fechaCorta; chip = chipSolicitud; ini = iniciales;
  editable = computed(() => { const e = this.s()?.estado_solicitud; return e === 1 || e === 2; });

  ngOnInit() { this.cargar(); }

  private cargar() {
    this.api.solicitudesDeMedico(this.auth.sesion()!.id_medico!).subscribe({
      next: lista => {
        const sol = lista.find(x => x.id_solicitud === Number(this.id()));
        if (!sol) { this.error.set('No se encontró la solicitud.'); this.cargando.set(false); return; }
        this.fijar(sol);
        this.api.ficha(sol.id_paciente).subscribe({ next: fi => this.ficha.set(fi), error: () => {} });
        if (sol.estado_solicitud === 1) {
          this.api.marcarEnRevision(sol.id_solicitud).subscribe({ next: n => this.fijar(n), error: e => this.error.set(mensajeError(e)) });
        }
        this.cargando.set(false);
      },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }

  private fijar(sol: Solicitud) {
    this.s.set(sol);
    if (!this.lineas().length) {
      this.lineas.set(sol.medicamentos.map(m => ({
        id_medicamento: m.id_medicamento, nombre: `${m.nombre_medicamento} ${m.concentracion ?? ''}`.trim(),
        dosis: '', frecuencia: '', fecha_inicio: hoyISO(), fecha_fin: enDias(30),
      })));
    }
  }

  aprobar() {
    const faltan = this.lineas().some(l => !l.dosis.trim() || !l.frecuencia.trim() || !l.fecha_inicio || !l.fecha_fin);
    if (faltan) { this.confirmar.set(false); this.error.set('Completa dosis, frecuencia y fechas de cada medicamento.'); return; }
    if (this.lineas().some(l => l.fecha_fin < l.fecha_inicio)) { this.confirmar.set(false); this.error.set('La fecha fin no puede ser anterior al inicio.'); return; }
    this.error.set(''); this.procesando.set(true);
    this.api.aprobar(this.s()!.id_solicitud, this.lineas().map(l => ({
      id_medicamento: l.id_medicamento, dosis: l.dosis.trim(), frecuencia: l.frecuencia.trim(), fecha_inicio: l.fecha_inicio, fecha_fin: l.fecha_fin,
    }))).subscribe({
      next: r => { this.procesando.set(false); this.confirmar.set(false); this.emitida.set(r); },
      error: e => { this.procesando.set(false); this.confirmar.set(false); this.error.set(mensajeError(e)); },
    });
  }

  rechazar() {
    if (!this.motivoRechazo.trim()) { this.errorRechazo.set('El motivo del rechazo es obligatorio.'); return; }
    this.errorRechazo.set(''); this.procesando.set(true);
    this.api.rechazar(this.s()!.id_solicitud, this.motivoRechazo.trim()).subscribe({
      next: () => { this.procesando.set(false); this.rechazando.set(false); this.router.navigate(['/medico/bandeja']); },
      error: e => { this.procesando.set(false); this.errorRechazo.set(mensajeError(e)); },
    });
  }
  volver() { this.router.navigate(['/medico/bandeja']); }
}
