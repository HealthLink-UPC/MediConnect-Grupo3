import { Component, input, output } from '@angular/core';
import { Receta } from '../core/models';
import { chipReceta, diasParaVencer, fechaCorta } from '../core/util';

/** Detalle de receta (Fig. 79 / 98): medicamentos, dosis, vigencia y código de verificación. */
@Component({
  selector: 'app-receta-modal',
  template: `
    <div class="fondo-modal" (click)="cerrar.emit()">
      <div class="modal" role="dialog" aria-modal="true" (click)="$event.stopPropagation()">
        <h2>Receta #{{ r().id_receta }}</h2>
        <div class="row between wrap" style="justify-content:center">
          <span class="chip {{ chip().clase }}">{{ chip().texto }}</span>
        </div>
        <dl class="resumen" style="margin-block:16px">
          <div><dt>Paciente</dt><b>{{ r().nombre_paciente }}</b></div>
          <div><dt>Médico</dt><b>{{ r().nombre_medico }}</b></div>
          <div><dt>Emisión</dt><b>{{ f(r().fecha_emision) }}</b></div>
          <div><dt>Vigente hasta</dt><b>{{ f(r().fecha_vencimiento) }}
            @if (dias() >= 0) { <span class="muted small">({{ dias() }} días)</span> }</b></div>
          <div><dt>Código</dt><b style="font-family:monospace">{{ r().codigo_verificacion }}</b></div>
        </dl>
        <h3>Medicamentos</h3>
        @for (d of r().detalles; track d.id_tratamiento) {
          <div class="item-med">
            <span class="icono-pastilla">💊</span>
            <div style="flex:1"><b>{{ d.nombre_medicamento }} {{ d.concentracion }}</b>
              <div class="muted">{{ d.dosis }} · {{ d.frecuencia }}</div>
              <div class="muted small">Del {{ f(d.fecha_inicio) }} al {{ f(d.fecha_fin) }}</div></div>
          </div>
        }
        <div class="acciones">
          @if (puedeRenovar()) { <button class="btn" (click)="renovar.emit()">Solicitar renovación</button> }
          <button class="btn sec" (click)="cerrar.emit()">Cerrar</button>
        </div>
      </div>
    </div>
  `,
})
export class RecetaModal {
  r = input.required<Receta>();
  puedeRenovar = input(false);
  cerrar = output<void>();
  renovar = output<void>();
  f = fechaCorta;
  chip = () => chipReceta(this.r());
  dias = () => diasParaVencer(this.r());
}
