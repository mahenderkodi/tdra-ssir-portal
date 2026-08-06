import {
  HttpErrorResponse,
  HttpInterceptorFn
} from '@angular/common/http';

import {
  inject
} from '@angular/core';

import {
  catchError,
  switchMap,
  throwError
} from 'rxjs';

import {
  AuthService
} from './auth-service';

import {
  TokenStorageService
} from './token-storage';


export const errorInterceptor:
  HttpInterceptorFn = (req, next) => {

  /*
   * Authentication requests must not inject
   * AuthService inside this interceptor.
   *
   * This prevents the circular dependency.
   */
  const isAuthenticationRequest =
    req.url.includes('/api/v1/auth/');

  if (isAuthenticationRequest) {
    return next(req);
  }


  /*
   * These services are injected only for
   * non-authentication requests.
   */
  const authService =
    inject(AuthService);

  const tokenService =
    inject(TokenStorageService);


  return next(req).pipe(
    catchError(
      (error: HttpErrorResponse) => {

        /*
         * When a protected request receives 401,
         * request a new access token.
         */
        if (error.status === 401) {

          return authService
            .refreshToken()
            .pipe(
              switchMap(() => {

                const freshToken =
                  tokenService.getAccessToken();

                /*
                 * The refresh succeeded but no new
                 * access token was received.
                 */
                if (!freshToken) {
                  authService.logout();

                  return throwError(
                    () =>
                      new Error(
                        'No access token received after refresh.'
                      )
                  );
                }


                /*
                 * Retry the original request using
                 * the newly received access token.
                 */
                const retriedRequest =
                  req.clone({
                    setHeaders: {
                      Authorization:
                        `Bearer ${freshToken}`
                    }
                  });


                return next(
                  retriedRequest
                );
              }),

              catchError(refreshError => {

                /*
                 * Refresh token is invalid or expired.
                 */
                authService.logout();

                return throwError(
                  () => refreshError
                );
              })
            );
        }


        return throwError(
          () => error
        );
      }
    )
  );
};