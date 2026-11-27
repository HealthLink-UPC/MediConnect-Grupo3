import { Component, input } from '@angular/core';

/** Marco de las pantallas de acceso: panel degradado a la izquierda + contenido a la derecha (Figs. 63–66, 88–90). */
@Component({
  selector: 'app-auth-marco',
  template: `
    <div class="auth">
      <aside class="auth-panel">
        <div class="auth-logo"><img src="img/favicon.png" alt=""> MediConnect</div>
        <img class="auth-foto" src="img/auth-doctora.webp" alt="Médica atendiendo a una paciente">
        <h2>{{ titulo() }}</h2>
        <p>{{ texto() }}</p>
        <div class="auth-seguro">🛡 Información clínica cifrada y confidencial</div>
      </aside>
      <section class="auth-form"><div><ng-content /></div></section>
    </div>
  `,
})
export class AuthMarco {
  titulo = input('Tus recetas, más simples y siempre acompañadas.');
  texto = input('Renueva tratamientos habituales y recibe seguimiento clínico sin traslados innecesarios.');
}
