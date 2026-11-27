import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
import { Rol } from './models';

/** Solo entra quien tenga sesión y el rol indicado; si no, va al login (o a su propio portal). */
export const rolGuard = (rol: Rol): CanActivateFn => () => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const s = auth.sesion();
  if (!s) return router.createUrlTree(['/login']);
  if (s.rol_usuario !== rol) return router.createUrlTree([auth.inicio()]);
  return true;
};

/** Login y registro: si ya hay sesión, redirige al portal correspondiente. */
export const invitadoGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.sesion() ? inject(Router).createUrlTree([auth.inicio()]) : true;
};
