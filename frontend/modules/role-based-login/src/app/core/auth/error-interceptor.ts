import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, switchMap, throwError } from 'rxjs';
import { AuthService } from './auth';
import { TokenStorageService } from './token-storage';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const tokenService = inject(TokenStorageService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      // If unauthorized, and we are not already hitting an authentication endpoint
      if (error.status === 401 && !req.url.includes('/api/v1/auth/')) {
        return authService.refreshToken().pipe(
          switchMap(() => {
            const freshToken = tokenService.accessToken();
            const retriedRequest = req.clone({
              setHeaders: {
                Authorization: `Bearer ${freshToken}`
              }
            });
            return next(retriedRequest); // Retry original request with fresh token
          }),
          catchError((refreshError) => {
            authService.logout(); // Refresh token has expired, terminate session
            return throwError(() => refreshError);
          })
        );
      }

      return throwError(() => error);
    })
  );
};