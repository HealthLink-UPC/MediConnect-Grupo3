import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { environment } from './environment';

/** Añade "Authorization: Bearer <token>" a cada llamada a la API y cierra sesión si el token venció (401). */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const esApi = req.url.startsWith(environment.apiUrl);
  const peticion = token && esApi ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;
  return next(peticion).pipe(
    catchError((e: HttpErrorResponse) => {
      if (e.status === 401 && token && !req.url.includes('/auth/login')) auth.cerrar(true);
      return throwError(() => e);
    }),
  );
};
