import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';

import { inject } from '@angular/core';

import {
  catchError,
  switchMap,
  throwError
} from 'rxjs';

import { AuthService } from './auth-service';
import { TokenStorageService } from './token-storage';

//req  = current API request
//next = send this request to the next step/backend
export const errorInterceptor:
HttpInterceptorFn = (req, next) => {

  // Auth APIs are excluded to avoid circular refresh/interceptor handling.
  const isAuthenticationRequest =
    req.url.includes('/api/v1/auth/');

  if (isAuthenticationRequest) {
    return next(req);
  }

  const authService = inject(AuthService);
  const tokenService = inject(TokenStorageService);


  return next(req).pipe(

    catchError((error: HttpErrorResponse) => {

      // A 401 from a protected API triggers access-token refresh.
      if (error.status === 401) {
        //Send the API request, then process what happens - pipe
        return authService
          .refreshToken()
          .pipe(

            switchMap(() => {

              const freshToken =
                tokenService.getAccessToken();

              if (!freshToken) {
                authService.logout();

                return throwError(
                  () =>
                    new Error(
                      'No access token received after refresh.'
                    )
                );
              }

              // Retry the failed API call with the new access token.
              const retriedRequest =
                req.clone({
                  setHeaders: {
                    Authorization:
                      `Bearer ${freshToken}`
                  }
                });

              return next(retriedRequest);
            }),

            // If refresh itself fails, end the current session.
            catchError(refreshError => {
              authService.logout();

              return throwError(
                () => refreshError
              );
            })
          );
      }

      return throwError(() => error);
    })
  );
};