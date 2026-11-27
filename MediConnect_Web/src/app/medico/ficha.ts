import { Component, input } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FichaClinicaView } from '../shared/ficha';

/** Ficha clínica del paciente para el médico (Fig. 100): solo lectura, porque el backend solo deja editar al propio paciente. */
@Component({
  selector: 'app-medico-ficha',
  imports: [RouterLink, FichaClinicaView],
  template: `
    <a routerLink="/medico/pacientes" class="small">← Volver a pacientes</a>
    <div class="pagina-cab mt"><h1>Ficha clínica del paciente</h1><p>Antecedentes, alergias y datos de seguridad (consulta).</p></div>
    <app-ficha [idPaciente]="+id()" [editable]="false" />
  `,
})
export class MedicoFicha {
  id = input.required<string>();
}
