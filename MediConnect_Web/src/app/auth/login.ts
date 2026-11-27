import { Component, OnDestroy, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { LoginRespuesta, Sesion } from '../core/models';
import { mensajeError } from '../core/util';
import { AuthMarco } from './auth-marco';

/**
 * Inicio de sesión en dos pasos: 1) correo y contraseña, 2) código de 6 dígitos que llega al correo.
 * Si el backend no exige el código, entra directo.
 */
@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, AuthMarco],
  template: `
    <app-auth-marco>
      @if (!auth.reto()) {
        <h1>Inicia sesión</h1>
        <p class="muted">Accede a tus tratamientos, solicitudes y recetas digitales.</p>

        @if (vencida) { <div class="aviso warn">Tu sesión venció. Inicia sesión nuevamente.</div> }

        <form (ngSubmit)="entrar()" novalidate>
          <label class="lbl" for="correo">Correo electrónico</label>
          <input id="correo" name="correo" class="input" type="email" [(ngModel)]="correo" autocomplete="username" placeholder="nombre@correo.com">

          <label class="lbl" for="clave">Contraseña</label>
          <div class="pw">
            <input id="clave" name="clave" class="input" [type]="ver() ? 'text' : 'password'" [(ngModel)]="clave" autocomplete="current-password">
            <button type="button" (click)="ver.set(!ver())">{{ ver() ? 'Ocultar' : 'Mostrar' }}</button>
          </div>

          <div class="row between" style="margin:14px 0 18px">
            <label class="check"><input type="checkbox" name="rec" [(ngModel)]="recordar"> Recordarme</label>
            <a routerLink="/recuperar" class="small">¿Olvidaste tu contraseña?</a>
          </div>

          @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }

          <button class="btn block" type="submit" [disabled]="cargando()">
            {{ cargando() ? 'Ingresando…' : '→ Iniciar Sesión' }}
          </button>
        </form>
        <p class="muted" style="text-align:center;margin-top:22px">¿Aún no tienes cuenta? <a routerLink="/registro">Crear cuenta</a></p>
      } @else {
        <h1>Verifica tu identidad</h1>
        <p class="muted">Enviamos un código de 6 dígitos a <b>{{ auth.reto()!.correo }}</b>. Vence en 5 minutos.</p>

        <form (ngSubmit)="verificar()" novalidate>
          <label class="lbl" for="codigo">Código de verificación</label>
          <input id="codigo" name="codigo" class="input" inputmode="numeric" autocomplete="one-time-code" maxlength="6"
                 style="font-size:24px;letter-spacing:.35em;text-align:center" placeholder="······" [(ngModel)]="codigo">

          @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
          @if (info()) { <div class="aviso ok" role="status">{{ info() }}</div> }

          <button class="btn block mt" type="submit" [disabled]="cargando()">{{ cargando() ? 'Verificando…' : '→ Verificar y entrar' }}</button>
        </form>

        <div class="row between mt">
          <button class="btn sec peq" type="button" (click)="volver()">← Volver</button>
          <button class="btn sec peq" type="button" [disabled]="espera() > 0 || cargando()" (click)="reenviar()">
            {{ espera() > 0 ? 'Reenviar código (' + espera() + ' s)' : 'Reenviar código' }}
          </button>
        </div>
        <p class="muted small mt">¿No lo ves? Revisa la carpeta de spam.</p>
      }
    </app-auth-marco>
  `,
})
export class LoginPage implements OnDestroy {
  private api = inject(ApiService);
  auth = inject(AuthService);
  private router = inject(Router);
  vencida = !!inject(ActivatedRoute).snapshot.queryParamMap.get('vencida');

  correo = ''; clave = ''; recordar = false; codigo = '';
  ver = signal(false);
  cargando = signal(false);
  error = signal('');
  info = signal('');
  espera = signal(0);
  private reloj: ReturnType<typeof setInterval> | null = null;

  constructor() { if (this.auth.reto()) this.iniciarEspera(); }
  ngOnDestroy() { this.detenerReloj(); }

  entrar() {
    if (!this.correo.trim() || !this.clave) { this.error.set('Ingresa tu correo y tu contraseña.'); return; }
    this.error.set(''); this.cargando.set(true);
    this.api.login(this.correo.trim(), this.clave).subscribe({
      next: r => this.recibir(r),
      error: e => {
        this.cargando.set(false);
        this.error.set(e.status === 401 || e.status === 400 ? 'Correo o contraseña incorrectos.' : mensajeError(e));
      },
    });
  }

  private recibir(r: LoginRespuesta) {
    this.cargando.set(false);
    if (r.requiere_codigo && r.id_reto) {
      this.auth.reto.set({ id: r.id_reto, correo: r.correo_enmascarado ?? 'tu correo' });
      this.codigo = ''; this.info.set(''); this.iniciarEspera();
      return;
    }
    this.auth.iniciar(r as Sesion, this.recordar);
    this.router.navigate([this.auth.inicio()]);
  }

  verificar() {
    const reto = this.auth.reto();
    if (!reto) return;
    if (!/^\d{6}$/.test(this.codigo.trim())) { this.error.set('Ingresa el código de 6 dígitos.'); return; }
    this.error.set(''); this.info.set(''); this.cargando.set(true);
    this.api.verificarCodigo(reto.id, this.codigo.trim()).subscribe({
      next: s => {
        this.auth.reto.set(null); this.detenerReloj();
        this.auth.iniciar(s, this.recordar);
        this.router.navigate([this.auth.inicio()]);
      },
      error: e => {
        this.cargando.set(false);
        this.error.set(mensajeError(e));
        // Sin reto vigente (venció o demasiados intentos): hay que empezar de nuevo
        if (e.status === 429 || (e.status === 401 && /venci/i.test(mensajeError(e)))) this.volver(mensajeError(e));
      },
    });
  }

  reenviar() {
    const reto = this.auth.reto();
    if (!reto) return;
    this.error.set(''); this.info.set(''); this.cargando.set(true);
    this.api.reenviarCodigo(reto.id).subscribe({
      next: () => { this.cargando.set(false); this.codigo = ''; this.info.set('Te enviamos un código nuevo.'); this.iniciarEspera(); },
      error: e => { this.cargando.set(false); this.error.set(mensajeError(e)); },
    });
  }

  volver(mensaje = '') {
    this.auth.reto.set(null); this.detenerReloj();
    this.codigo = ''; this.clave = ''; this.info.set(''); this.error.set(mensaje);
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
