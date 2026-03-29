import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  const isAuthEndpoint =
    req.url.includes('/auth/login') ||
    req.url.includes('/auth/register') ||
    req.url.includes('/auth/refresh-token') ||
    req.url.includes('/auth/logout');

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401 && !isAuthEndpoint) {
        const refreshToken = authService.getRefreshToken();

        if (!refreshToken) {
          authService.logout().subscribe();
          return throwError(() => error);
        }

        return authService.refreshToken().pipe(
          switchMap((newAccessToken) => {
            const retryReq = req.clone({
              setHeaders: {
                Authorization: `Bearer ${newAccessToken}`
              }
            });

            return next(retryReq);
          }),
          catchError((refreshError) => {
            authService.logout().subscribe();
            return throwError(() => refreshError);
          })
        );
      }

      if (error.status === 403) {
        router.navigate(['/dashboard']);
      }

      return throwError(() => error);
    })
  );
};