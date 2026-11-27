import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Sesion } from '../core/models';
import { erroresPorCampo, hoyISO, mensajeError } from '../core/util';
import { AuthMarco } from './auth-marco';

type Paso = 'rol' | 'form' | 'listo';

/** Selección de rol (Fig. 67) → registro de paciente / médico (Figs. 68–69) → cuenta creada (Fig. 90). */
@Component({
  selector: 'app-registro',
  imports: [FormsModule, RouterLink, AuthMarco],
  template: `
    <app-auth-marco [titulo]="rol() === 'MEDICO' ? 'Tu práctica clínica, conectada y segura.' : 'Tus recetas, más simples y siempre acompañadas.'"
      [texto]="rol() === 'MEDICO' ? 'Gestiona pacientes, revisa antecedentes y emite renovaciones con trazabilidad profesional.' : 'Renueva tratamientos habituales y recibe seguimiento clínico sin traslados innecesarios.'">

      @switch (paso()) {
        @case ('rol') {
          <h1>Crear cuenta</h1>
          <p class="muted">¿Cómo vas a usar MediConnect?</p>
          <div style="display:grid;gap:14px;margin-top:24px">
            <button class="rol-card" type="button" (click)="elegir('PACIENTE')">
              <img src="img/paciente.png" alt=""><span><b>Soy paciente</b><span class="muted">Renueva tus recetas y haz seguimiento de tus tratamientos.</span></span>
            </button>
            <button class="rol-card" type="button" (click)="elegir('MEDICO')">
              <img src="img/medico.png" alt=""><span><b>Soy médico</b><span class="muted">Revisa solicitudes y emite recetas con trazabilidad.</span></span>
            </button>
          </div>
          <p class="muted" style="text-align:center;margin-top:22px">¿Ya tienes cuenta? <a routerLink="/login">Iniciar sesión</a></p>
        }

        @case ('form') {
          <h1>{{ rol() === 'MEDICO' ? 'Registro de médico' : 'Registro de paciente' }}</h1>
          <p class="muted">Completa tus datos para crear tu cuenta.</p>
          <form (ngSubmit)="registrar()" novalidate>
            <div class="fila">
              <div><label class="lbl">Nombres</label><input class="input" name="nombre" [(ngModel)]="f.nombre_usuario" [class.invalid]="err('nombre_usuario')">
                @if (err('nombre_usuario')) { <div class="campo-error">{{ err('nombre_usuario') }}</div> }</div>
              <div><label class="lbl">Apellidos</label><input class="input" name="apellido" [(ngModel)]="f.apellido_usuario" [class.invalid]="err('apellido_usuario')">
                @if (err('apellido_usuario')) { <div class="campo-error">{{ err('apellido_usuario') }}</div> }</div>
            </div>
            <div class="fila">
              <div><label class="lbl">DNI</label><input class="input" name="dni" inputmode="numeric" maxlength="8" [(ngModel)]="f.dni" [class.invalid]="err('dni')">
                @if (err('dni')) { <div class="campo-error">{{ err('dni') }}</div> }</div>
              <div><label class="lbl">Fecha de nacimiento</label><input class="input" name="fn" type="date" [(ngModel)]="f.fecha_nacimiento" [class.invalid]="err('fecha_nacimiento')">
                @if (err('fecha_nacimiento')) { <div class="campo-error">{{ err('fecha_nacimiento') }}</div> }</div>
            </div>
            <div class="fila">
              <div><label class="lbl">Teléfono</label><input class="input" name="tel" inputmode="numeric" maxlength="9" placeholder="9 dígitos" [(ngModel)]="f.telefono" [class.invalid]="err('telefono')">
                @if (err('telefono')) { <div class="campo-error">{{ err('telefono') }}</div> }</div>
              <div><label class="lbl">Correo electrónico</label><input class="input" name="mail" type="email" autocomplete="username" [(ngModel)]="f.correo_usuario" [class.invalid]="err('correo_usuario')">
                @if (err('correo_usuario')) { <div class="campo-error">{{ err('correo_usuario') }}</div> }</div>
            </div>

            @if (rol() === 'MEDICO') {
              <div class="fila">
                <div><label class="lbl">Especialidad</label><input class="input" name="esp" [(ngModel)]="f.especialidad" [class.invalid]="err('especialidad')">
                  @if (err('especialidad')) { <div class="campo-error">{{ err('especialidad') }}</div> }</div>
                <div><label class="lbl">CMP (colegiatura)</label><input class="input" name="cmp" maxlength="10" [(ngModel)]="f.cmp" [class.invalid]="err('cmp')">
                  @if (err('cmp')) { <div class="campo-error">{{ err('cmp') }}</div> }</div>
              </div>
              <label class="lbl">Centro de atención</label><input class="input" name="centro" [(ngModel)]="f.centro_atencion" [class.invalid]="err('centro_atencion')">
              @if (err('centro_atencion')) { <div class="campo-error">{{ err('centro_atencion') }}</div> }
            }

            <div class="fila">
              <div><label class="lbl">Contraseña</label>
                <input class="input" name="clave" type="password" autocomplete="new-password" placeholder="Mínimo 12 caracteres" [(ngModel)]="f.clave_usuario" [class.invalid]="err('clave_usuario')">
                @if (err('clave_usuario')) { <div class="campo-error">{{ err('clave_usuario') }}</div> }</div>
              <div><label class="lbl">Confirmar contraseña</label>
                <input class="input" name="clave2" type="password" autocomplete="new-password" [(ngModel)]="confirmar" [class.invalid]="err('confirmar')">
                @if (err('confirmar')) { <div class="campo-error">{{ err('confirmar') }}</div> }</div>
            </div>

            @if (error()) { <div class="aviso error" role="alert">{{ error() }}</div> }
            <div class="row mt">
              <button class="btn sec" type="button" (click)="paso.set('rol')">Atrás</button>
              <button class="btn" style="flex:1" type="submit" [disabled]="cargando()">{{ cargando() ? 'Creando cuenta…' : 'Crear cuenta' }}</button>
            </div>
          </form>
        }

        @case ('listo') {
          <h1>¡Cuenta creada!</h1>
          <p class="muted">Tu registro se completó correctamente.</p>
          <div class="exito">
            <div class="tic">✓</div>
            <h3>Bienvenido/a, {{ rol() === 'MEDICO' ? 'Dr(a). ' : '' }}{{ f.nombre_usuario }}</h3>
            <p style="margin-top:10px">Tu cuenta con el correo <b>{{ f.correo_usuario }}</b> ya está activa.</p>
          </div>
          <ul class="pasos">
            @if (rol() === 'MEDICO') {
              <li>Revisa tus datos de colegiatura / CMP y especialidad</li>
              <li>Revisa las solicitudes de renovación pendientes en tu bandeja</li>
            } @else {
              <li>Completa tu perfil y revisa tus recetas</li>
              <li>Solicita la renovación de tus tratamientos habituales</li>
            }
          </ul>
          @if (error()) { <div class="aviso error">{{ error() }}</div> }
          <button class="btn block" (click)="irAlPanel()" [disabled]="cargando()">→ Ir a mi panel</button>
        }
      }
    </app-auth-marco>
  `,
})
export class RegistroPage {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);

  paso = signal<Paso>('rol');
  rol = signal<'PACIENTE' | 'MEDICO'>('PACIENTE');
  cargando = signal(false);
  error = signal('');
  campos = signal<Record<string, string>>({});
  confirmar = '';
  f = { nombre_usuario: '', apellido_usuario: '', dni: '', fecha_nacimiento: '', telefono: '', correo_usuario: '', clave_usuario: '',
        especialidad: '', cmp: '', centro_atencion: '' };

  err = (c: string) => this.campos()[c];

  elegir(r: 'PACIENTE' | 'MEDICO') { this.rol.set(r); this.paso.set('form'); this.error.set(''); this.campos.set({}); }

  private validar(): Record<string, string> {
    const e: Record<string, string> = {};
    const f = this.f;
    if (!f.nombre_usuario.trim()) e['nombre_usuario'] = 'El nombre es obligatorio';
    if (!f.apellido_usuario.trim()) e['apellido_usuario'] = 'El apellido es obligatorio';
    if (!/^\d{8}$/.test(f.dni)) e['dni'] = 'El DNI debe tener 8 dígitos';
    if (!f.fecha_nacimiento) e['fecha_nacimiento'] = 'La fecha de nacimiento es obligatoria';
    else if (f.fecha_nacimiento >= hoyISO()) e['fecha_nacimiento'] = 'La fecha debe ser anterior a hoy';
    if (!/^\d{9}$/.test(f.telefono)) e['telefono'] = 'El teléfono debe tener 9 dígitos';
    if (!/^\S+@\S+\.\S+$/.test(f.correo_usuario)) e['correo_usuario'] = 'El correo no tiene un formato válido';
    if (f.clave_usuario.length < 12 || f.clave_usuario.length > 50) e['clave_usuario'] = 'La contraseña debe tener entre 12 y 50 caracteres';
    if (this.confirmar !== f.clave_usuario) e['confirmar'] = 'Las contraseñas no coinciden';
    if (this.rol() === 'MEDICO') {
      if (!f.especialidad.trim()) e['especialidad'] = 'La especialidad es obligatoria';
      if (!f.cmp.trim()) e['cmp'] = 'El CMP es obligatorio';
      if (!f.centro_atencion.trim()) e['centro_atencion'] = 'El centro de atención es obligatorio';
    }
    return e;
  }

  registrar() {
    const e = this.validar();
    this.campos.set(e);
    if (Object.keys(e).length) { this.error.set('Revisa los campos marcados.'); return; }
    this.error.set(''); this.cargando.set(true);
    const base = { ...this.f };
    const llamada = this.rol() === 'MEDICO'
      ? this.api.registrarMedico(base)
      : this.api.registrarPaciente({ nombre_usuario: base.nombre_usuario, apellido_usuario: base.apellido_usuario, dni: base.dni,
          fecha_nacimiento: base.fecha_nacimiento, telefono: base.telefono, correo_usuario: base.correo_usuario, clave_usuario: base.clave_usuario });
    llamada.subscribe({
      next: () => { this.cargando.set(false); this.paso.set('listo'); },
      error: x => {
        this.cargando.set(false);
        const porCampo = erroresPorCampo(x);
        if (Object.keys(porCampo).length) { this.campos.set(porCampo); this.error.set('Revisa los campos marcados.'); }
        else this.error.set(mensajeError(x));
      },
    });
  }

  irAlPanel() {
    this.cargando.set(true); this.error.set('');
    this.api.login(this.f.correo_usuario.trim(), this.f.clave_usuario).subscribe({
      next: r => {
        // Con verificación en dos pasos, el login pide el código: se continúa en la pantalla de inicio de sesión
        if (r.requiere_codigo && r.id_reto) {
          this.auth.reto.set({ id: r.id_reto, correo: r.correo_enmascarado ?? 'tu correo' });
          this.router.navigate(['/login']);
          return;
        }
        this.auth.iniciar(r as Sesion, false);
        this.router.navigate([this.auth.inicio()]);
      },
      error: x => { this.cargando.set(false); this.error.set(mensajeError(x)); },
    });
  }
}
