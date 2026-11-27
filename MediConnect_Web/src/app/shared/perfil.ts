import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Perfil } from '../core/models';
import { FichaClinicaView } from './ficha';
import { erroresPorCampo, fechaCorta, iniciales, mensajeError } from '../core/util';

/** Mi perfil (Figs. 81 / 104): datos personales de solo lectura; el correo y el teléfono son editables. */
@Component({
  selector: 'app-perfil',
  imports: [FormsModule, FichaClinicaView],
  template: `
    <div class="pagina-cab"><h1>Mi perfil</h1><p>Tus datos personales, de contacto y{{ auth.esPaciente() ? ' tu ficha clínica' : ' seguridad' }}.</p></div>
    @if (cargando()) { <p class="muted">Cargando…</p> }
    @else if (perfil(); as p) {
      <div class="card" style="max-width:760px">
        <div class="row">
          <span class="avatar" style="width:64px;height:64px;font-size:20px">{{ ini(p) }}</span>
          <div><h2>{{ p.nombre_usuario }} {{ p.apellido_usuario }}</h2><span class="chip chip-blue">{{ p.rol_usuario }}</span></div>
        </div>
        <div class="grid-2 mt-lg">
          <div class="dato"><span>DNI</span><b>{{ p.dni }}</b></div>
          <div class="dato"><span>Fecha de nacimiento</span><b>{{ f(p.fecha_nacimiento) }}</b></div>
        </div>
        <form (ngSubmit)="guardar()" novalidate>
          <div class="fila">
            <div><label class="lbl" for="co">Correo electrónico</label>
              <input id="co" name="co" class="input" type="email" [(ngModel)]="p.correo_usuario" [class.invalid]="campos()['correo_usuario']">
              @if (campos()['correo_usuario']) { <div class="campo-error">{{ campos()['correo_usuario'] }}</div> }</div>
            <div><label class="lbl" for="te">Teléfono</label>
              <input id="te" name="te" class="input" inputmode="numeric" maxlength="9" [(ngModel)]="p.telefono" [class.invalid]="campos()['telefono']">
              @if (campos()['telefono']) { <div class="campo-error">{{ campos()['telefono'] }}</div> }</div>
          </div>
          @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
          @if (ok()) { <div class="aviso ok" role="status">Datos actualizados correctamente.</div> }
          <div class="row mt"><button class="btn" type="submit" [disabled]="guardando()">{{ guardando() ? 'Guardando…' : 'Guardar cambios' }}</button></div>
        </form>
      </div>

      @if (auth.esPaciente()) {
        <div style="max-width:760px">
          <h2 class="mt-lg" style="margin-bottom:14px">Mi ficha clínica</h2>
          <app-ficha [idPaciente]="auth.sesion()!.id_paciente!" [editable]="true" />
        </div>
      }

      <form class="card" style="max-width:760px" (ngSubmit)="cambiarClave()" novalidate>
        <h3>Cambiar contraseña</h3>
        <p class="muted mt">Mínimo 12 caracteres. Necesitas tu contraseña actual para confirmar el cambio.</p>
        <label class="lbl" for="ca">Contraseña actual</label>
        <input id="ca" name="ca" class="input" type="password" autocomplete="current-password" [(ngModel)]="claveActual">
        <div class="fila">
          <div><label class="lbl" for="cn">Nueva contraseña</label>
            <input id="cn" name="cn" class="input" type="password" autocomplete="new-password" [(ngModel)]="claveNueva"></div>
          <div><label class="lbl" for="cc">Confirmar nueva contraseña</label>
            <input id="cc" name="cc" class="input" type="password" autocomplete="new-password" [(ngModel)]="claveConfirmar"></div>
        </div>
        @if (errorClave()) { <div class="aviso error" role="alert">{{ errorClave() }}</div> }
        @if (okClave()) { <div class="aviso ok" role="status">Contraseña actualizada correctamente. Úsala la próxima vez que inicies sesión.</div> }
        <div class="row mt"><button class="btn" type="submit" [disabled]="cambiando()">{{ cambiando() ? 'Guardando…' : 'Cambiar contraseña' }}</button></div>
      </form>

      <div class="card" style="max-width:760px">
        <h3>Zona de cuenta</h3>
        <p class="muted mt">Si das de baja tu cuenta ya no podrás ingresar. Tus datos se conservan para fines de trazabilidad clínica.</p>
        <div class="row mt">
          <button class="btn peligro" (click)="confirmando.set(true)">Dar de baja mi cuenta</button>
          <button class="btn sec" (click)="auth.cerrar()">Cerrar sesión</button>
        </div>
      </div>
    } @else if (error()) { <div class="aviso error">{{ error() }}</div> }

    @if (confirmando()) {
      <div class="fondo-modal" (click)="confirmando.set(false)"><div class="modal" role="alertdialog" aria-modal="true" (click)="$event.stopPropagation()">
        <h2>¿Dar de baja tu cuenta?</h2>
        <p class="muted" style="text-align:center">Se cerrará tu sesión y no podrás volver a ingresar con esta cuenta.</p>
        <div class="acciones">
          <button class="btn peligro" (click)="baja()">Sí, dar de baja</button>
          <button class="btn sec" (click)="confirmando.set(false)">Cancelar</button>
        </div>
      </div></div>
    }
  `,
})
export class PerfilPage {
  private api = inject(ApiService);
  auth = inject(AuthService);
  perfil = signal<Perfil | null>(null);
  cargando = signal(true);
  guardando = signal(false);
  confirmando = signal(false);
  ok = signal(false);
  error = signal('');
  campos = signal<Record<string, string>>({});
  cambiando = signal(false);
  okClave = signal(false);
  errorClave = signal('');
  claveActual = ''; claveNueva = ''; claveConfirmar = '';
  f = fechaCorta;
  ini = (p: Perfil) => iniciales(`${p.nombre_usuario} ${p.apellido_usuario}`);

  constructor() {
    this.api.perfil(this.auth.sesion()!.id_usuario).subscribe({
      next: p => { this.perfil.set(p); this.cargando.set(false); },
      error: e => { this.error.set(mensajeError(e)); this.cargando.set(false); },
    });
  }

  guardar() {
    const p = this.perfil(); if (!p) return;
    this.ok.set(false); this.error.set(''); this.campos.set({}); this.guardando.set(true);
    this.api.actualizarPerfil(p.id_usuario, p).subscribe({
      next: n => { this.perfil.set(n); this.guardando.set(false); this.ok.set(true); },
      error: e => {
        this.guardando.set(false);
        const c = erroresPorCampo(e);
        if (Object.keys(c).length) this.campos.set(c); else this.error.set(mensajeError(e));
      },
    });
  }

  cambiarClave() {
    this.okClave.set(false); this.errorClave.set('');
    if (!this.claveActual) { this.errorClave.set('Ingresa tu contraseña actual.'); return; }
    if (this.claveNueva.length < 12 || this.claveNueva.length > 50) { this.errorClave.set('La nueva contraseña debe tener entre 12 y 50 caracteres.'); return; }
    if (this.claveNueva !== this.claveConfirmar) { this.errorClave.set('Las contraseñas nuevas no coinciden.'); return; }
    if (this.claveNueva === this.claveActual) { this.errorClave.set('La nueva contraseña debe ser distinta de la actual.'); return; }
    this.cambiando.set(true);
    this.api.cambiarClave(this.auth.sesion()!.id_usuario, this.claveActual, this.claveNueva).subscribe({
      next: () => { this.cambiando.set(false); this.okClave.set(true); this.claveActual = ''; this.claveNueva = ''; this.claveConfirmar = ''; },
      error: e => {
        this.cambiando.set(false);
        const c = erroresPorCampo(e);
        this.errorClave.set(Object.keys(c).length ? Object.values(c).join(' · ') : mensajeError(e));
      },
    });
  }

  baja() {
    this.api.darDeBaja(this.auth.sesion()!.id_usuario).subscribe({
      next: () => this.auth.cerrar(),
      error: e => { this.confirmando.set(false); this.error.set(mensajeError(e)); },
    });
  }
}
