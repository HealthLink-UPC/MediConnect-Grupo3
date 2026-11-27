import { Component, OnDestroy, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { erroresPorCampo, mensajeError } from '../core/util';
import { AuthMarco } from './auth-marco';

type Paso = 'correo' | 'codigo' | 'clave' | 'listo';

/** Recuperación de contraseña (Fig. 65): 1) correo, 2) código que llega al correo, 3) nueva contraseña. */
@Component({
  selector: 'app-recuperar',
  imports: [FormsModule, RouterLink, AuthMarco],
  template: `
    <app-auth-marco>
      <h1>Recuperar contraseña</h1>
      @switch (paso()) {
        @case ('correo') {
          <p class="muted">Ingresa tu correo y te enviaremos un código para restablecer tu contraseña.</p>
          <form (ngSubmit)="enviar()" novalidate>
            <label class="lbl" for="c">Correo electrónico</label>
            <input id="c" name="c" class="input" type="email" [(ngModel)]="correo" placeholder="nombre@correo.com">
            @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
            <button class="btn block mt" type="submit" [disabled]="cargando()">{{ cargando() ? 'Enviando…' : 'Enviar código' }}</button>
          </form>
        }
        @case ('codigo') {
          <p class="muted">Si el correo <b>{{ enmascarado() }}</b> está registrado, te enviamos un código de 6 dígitos. Vence en 5 minutos.</p>
          <form (ngSubmit)="continuar()" novalidate>
            <label class="lbl" for="cod">Código de verificación</label>
            <input id="cod" name="cod" class="input" inputmode="numeric" autocomplete="one-time-code" maxlength="6"
                   style="font-size:22px;letter-spacing:.35em;text-align:center" placeholder="······" [(ngModel)]="codigo">
            @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
            @if (info()) { <div class="aviso ok" role="status">{{ info() }}</div> }
            <button class="btn block mt" type="submit" [disabled]="cargando()">{{ cargando() ? 'Verificando…' : 'Continuar' }}</button>
          </form>
          <div class="row between mt">
            <button class="btn sec peq" type="button" (click)="paso.set('correo')">← Cambiar correo</button>
            <button class="btn sec peq" type="button" [disabled]="espera() > 0 || cargando()" (click)="reenviar()">
              {{ espera() > 0 ? 'Reenviar código (' + espera() + ' s)' : 'Reenviar código' }}
            </button>
          </div>
          <p class="muted small mt">¿No lo ves? Revisa la carpeta de spam.</p>
        }
        @case ('clave') {
          <p class="muted">Código verificado. Ahora elige tu nueva contraseña.</p>
          <form (ngSubmit)="restablecer()" novalidate>
            <label class="lbl" for="n1">Nueva contraseña</label>
            <input id="n1" name="n1" class="input" type="password" autocomplete="new-password" placeholder="Mínimo 12 caracteres" [(ngModel)]="nueva">
            <label class="lbl" for="n2">Confirmar nueva contraseña</label>
            <input id="n2" name="n2" class="input" type="password" autocomplete="new-password" [(ngModel)]="confirmar">
            @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
            @if (info()) { <div class="aviso ok" role="status">{{ info() }}</div> }
            <button class="btn block mt" type="submit" [disabled]="cargando()">{{ cargando() ? 'Guardando…' : 'Cambiar contraseña' }}</button>
          </form>
        }
        @case ('listo') {
          <div class="aviso ok mt">Tu contraseña se cambió correctamente. Ya puedes iniciar sesión con la nueva.</div>
          <a class="btn block mt" routerLink="/login">→ Ir a iniciar sesión</a>
        }
      }
      @if (paso() !== 'listo') {
        <p style="text-align:center;margin-top:22px"><a routerLink="/login">← Volver a iniciar sesión</a></p>
      }
    </app-auth-marco>
  `,
})
export class RecuperarPage implements OnDestroy {
  private api = inject(ApiService);
  paso = signal<Paso>('correo');
  cargando = signal(false);
  error = signal('');
  info = signal('');
  espera = signal(0);
  enmascarado = signal('');
  correo = ''; codigo = ''; nueva = ''; confirmar = '';
  private idReto = '';
  private reloj: ReturnType<typeof setInterval> | null = null;

  ngOnDestroy() { this.detenerReloj(); }

  enviar() {
    if (!/^\S+@\S+\.\S+$/.test(this.correo.trim())) { this.error.set('Ingresa un correo válido.'); return; }
    this.pedirCodigo(false);
  }

  reenviar() { this.pedirCodigo(true); }

  private pedirCodigo(reenvio: boolean) {
    this.error.set(''); this.info.set(''); this.cargando.set(true);
    this.api.recuperar(this.correo.trim()).subscribe({
      next: r => {
        this.cargando.set(false);
        this.idReto = r.id_reto ?? '';
        this.enmascarado.set(r.correo_enmascarado ?? this.correo);
        this.codigo = '';
        if (reenvio) this.info.set('Te enviamos un código nuevo.');
        this.paso.set('codigo'); this.iniciarEspera();
      },
      error: e => { this.cargando.set(false); this.error.set(mensajeError(e)); },
    });
  }

  continuar() {
    this.error.set(''); this.info.set('');
    if (!/^\d{6}$/.test(this.codigo.trim())) { this.error.set('Ingresa el código de 6 dígitos.'); return; }
    this.cargando.set(true);
    this.api.verificarRecuperacion(this.idReto, this.codigo.trim()).subscribe({
      next: () => { this.cargando.set(false); this.detenerReloj(); this.paso.set('clave'); },
      error: e => {
        this.cargando.set(false);
        this.error.set(mensajeError(e));
        if (e.status === 429 || (e.status === 401 && /venci/i.test(mensajeError(e)))) this.paso.set('correo');
      },
    });
  }

  restablecer() {
    this.error.set(''); this.info.set('');
    if (this.nueva.length < 12 || this.nueva.length > 50) { this.error.set('La nueva contraseña debe tener entre 12 y 50 caracteres.'); return; }
    if (this.nueva !== this.confirmar) { this.error.set('Las contraseñas no coinciden.'); return; }
    this.cargando.set(true);
    this.api.restablecer(this.idReto, this.codigo.trim(), this.nueva).subscribe({
      next: () => { this.cargando.set(false); this.detenerReloj(); this.nueva = ''; this.confirmar = ''; this.paso.set('listo'); },
      error: e => {
        this.cargando.set(false);
        const c = erroresPorCampo(e);
        this.error.set(Object.keys(c).length ? Object.values(c).join(' · ') : mensajeError(e));
        // Sin reto vigente (venció o demasiados intentos): hay que pedir otro código
        if (e.status === 429 || (e.status === 401 && /venci/i.test(mensajeError(e)))) this.paso.set('correo');
      },
    });
  }

  private iniciarEspera() {
    this.detenerReloj();
    this.espera.set(30);
    this.reloj = setInterval(() => {
      this.espera.update(n => Math.max(0, n - 1));
      if (this.espera() === 0) this.detenerReloj();
    }, 1000);
  }
  private detenerReloj() { if (this.reloj) { clearInterval(this.reloj); this.reloj = null; } }
}
