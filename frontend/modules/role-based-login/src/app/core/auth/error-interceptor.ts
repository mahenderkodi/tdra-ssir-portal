/*errorInterceptor watches protected API calls for a 401 
Unauthorized; when that happens, it uses the refresh token
to get a new access token, retries the failed request with 
the new token, and if refresh fails, it logs the user out 
and propagates the error.*/
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
import { LoggerService } from '../../layouts/logging/loggerService';

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
    const logger = inject(LoggerService);

    return next(req).pipe(

      catchError((error: HttpErrorResponse) => {

        // A 401(unauthorized) from a protected API triggers access-token refresh. 
        if (error.status === 401) {

          logger.warn(
            'Protected request returned 401; attempting token refresh'
          );
          //Send the API request, then process what happens - pipe
          return authService
            .refreshToken()
            .pipe(

              // If refresh itself fails, end the current session.

              catchError(refreshError => {
                logger.error(
                  'Token refresh failed; authentication session cleared'
                );
                authService.logout();

                return throwError(
                  () => refreshError
                );
              }),
              // switch to executing original HTTP request again
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


            );
        }

        return throwError(() => error);
      })
    );
  };