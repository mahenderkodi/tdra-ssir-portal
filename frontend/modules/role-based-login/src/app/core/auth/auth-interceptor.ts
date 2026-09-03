import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { TokenStorageService } from './token-storage';
import { environment } from '../../../environments/environment';


/*
authInterceptor checks outgoing requests to the application's backend API.

For protected endpoints:
- Retrieves the current access token.
- Adds the token as a Bearer Authorization header.

For public authentication endpoints:
- Sends the request without an access token.

Requests to external/third-party URLs are left unchanged.
*/

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const tokenStorage = inject(TokenStorageService);
  const accessToken = tokenStorage.getAccessToken();


  // Check whether the request belongs to our backend API.
  const isApiRequest =
    req.url.startsWith(environment.apiBaseUrl);


  // Authentication endpoints that do not require an existing access token.
  const isPublicAuthRequest =
    req.url.includes('/auth/login') ||
    req.url.includes('/auth/register-init') ||
    req.url.includes('/auth/refresh') ||
    req.url.includes('/auth/forgot-password');


  // Attach JWT only to protected backend API requests.
  if (
    isApiRequest &&
    accessToken &&
    !isPublicAuthRequest
  ) {

    // Angular HTTP requests are immutable,
    // so clone the request before adding headers.
    const authenticatedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      }
    });

    return next(authenticatedRequest);
  }


  return next(req);
};