import { Component, input, output } from '@angular/core';
import { Solicitud } from '../core/models';
import { chipSolicitud, fechaCorta } from '../core/util';

/** Detalle de solicitud con su estado (Figs. 76–78). */
@Component({
  selector: 'app-solicitud-modal',
  template: `
    <div class="fondo-modal" (click)="cerrar.emit()">
      <div class="modal" role="dialog" aria-modal="true" (click)="$event.stopPropagation()">
        <h2>Solicitud #{{ s().id_solicitud }}</h2>
        <div style="text-align:center"><span class="chip {{ chip().clase }}">{{ chip().texto }}</span></div>
        <dl class="resumen">
          <div><dt>Tipo</dt><b>{{ s().tipo_solicitud }}</b></div>
          <div><dt>Fecha</dt><b>{{ f(s().fecha_solicitud) }}</b></div>
          <div><dt>Paciente</dt><b>{{ s().nombre_paciente }}</b></div>
          <div><dt>Médico</dt><b>{{ s().nombre_medico }}</b></div>
          @if (s().medicamentos.length) {
            <div><dt>Medicamentos</dt><b>@for (m of s().medicamentos; track m.id_medicamento) { <div>{{ m.nombre_medicamento }} {{ m.concentracion }}</div> }</b></div>
          }
          @if (s().motivo) { <div><dt>Comentario</dt><b>{{ s().motivo }}</b></div> }
        </dl>
        @if (s().estado_solicitud === 4 && s().motivo_estado) {
          <div class="aviso error"><b>Motivo del rechazo:</b> {{ s().motivo_estado }}</div>
        }
        @if (s().estado_solicitud === 3 && s().id_receta) {
          <div class="aviso ok">Tu receta ya fue emitida. Puedes verla en «Mis recetas».</div>
        }
        <div class="acciones">
          @if (s().estado_solicitud === 3 && s().id_receta) { <button class="btn" (click)="verReceta.emit(s().id_receta!)">Ver receta</button> }
          <button class="btn sec" (click)="cerrar.emit()">Cerrar</button>
        </div>
      </div>
    </div>
  `,
})
export class SolicitudModal {
  s = input.required<Solicitud>();
  cerrar = output<void>();
  verReceta = output<number>();
  f = fechaCorta;
  chip = () => chipSolicitud(this.s());
}
