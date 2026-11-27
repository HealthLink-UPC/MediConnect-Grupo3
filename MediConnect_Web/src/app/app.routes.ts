import { Routes } from '@angular/router';
import { invitadoGuard, rolGuard } from './core/guards';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'login' },
  {
    path: 'login', canActivate: [invitadoGuard],
    loadComponent: () => import('./auth/login').then(m => m.LoginPage),
  },
  {
    path: 'registro', canActivate: [invitadoGuard],
    loadComponent: () => import('./auth/registro').then(m => m.RegistroPage),
  },
  {
    path: 'recuperar', canActivate: [invitadoGuard],
    loadComponent: () => import('./auth/recuperar').then(m => m.RecuperarPage),
  },
  {
    path: 'paciente', canActivate: [rolGuard('PACIENTE')],
    loadComponent: () => import('./layout/portal').then(m => m.PortalLayout),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'inicio' },
      { path: 'inicio', loadComponent: () => import('./paciente/inicio').then(m => m.PacienteInicio) },
      { path: 'recetas', loadComponent: () => import('./paciente/recetas').then(m => m.PacienteRecetas) },
      { path: 'solicitudes', loadComponent: () => import('./paciente/solicitudes').then(m => m.PacienteSolicitudes) },
      { path: 'solicitar', loadComponent: () => import('./paciente/solicitar').then(m => m.PacienteSolicitar) },
      { path: 'historial', loadComponent: () => import('./paciente/historial').then(m => m.PacienteHistorial) },
      { path: 'perfil', loadComponent: () => import('./shared/perfil').then(m => m.PerfilPage) },
    ],
  },
  {
    path: 'medico', canActivate: [rolGuard('MEDICO')],
    loadComponent: () => import('./layout/portal').then(m => m.PortalLayout),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'bandeja' },
      { path: 'bandeja', loadComponent: () => import('./medico/bandeja').then(m => m.MedicoBandeja) },
      { path: 'revision/:id', loadComponent: () => import('./medico/revision').then(m => m.MedicoRevision) },
      { path: 'pacientes', loadComponent: () => import('./medico/pacientes').then(m => m.MedicoPacientes) },
      { path: 'pacientes/:id', loadComponent: () => import('./medico/ficha').then(m => m.MedicoFicha) },
      { path: 'catalogo', loadComponent: () => import('./medico/catalogo').then(m => m.MedicoCatalogo) },
      { path: 'recetas', loadComponent: () => import('./medico/recetas').then(m => m.MedicoRecetas) },
      { path: 'perfil', loadComponent: () => import('./shared/perfil').then(m => m.PerfilPage) },
    ],
  },
  { path: '**', redirectTo: 'login' },
];
