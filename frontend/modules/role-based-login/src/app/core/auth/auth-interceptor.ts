import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenStorageService } from './token-storage';


/* authInterceptor checks each outgoing HttpClient request and, for 
protected endpoints, clones the request and attaches the current access 
token as a Bearer Authorization header; if no access token exists, it 
simply forwards the request unchanged.*/

export const authInterceptor: HttpInterceptorFn = (req, next) => {

  const tokenStorage = inject(TokenStorageService);
  const accessToken = tokenStorage.getAccessToken();

  // Auth endpoints that must work without an existing access token.
  const isPublicAuthRequest =
    req.url.includes('/api/v1/auth/login') ||
    req.url.includes('/api/v1/auth/register-init') ||
    req.url.includes('/api/v1/auth/refresh') ||
    req.url.includes('/api/v1/auth/forgot-password');

  // Automatically attaches the JWT to protected API requests.
  if (accessToken && !isPublicAuthRequest) {
    //Angular HTTP requests are immutable.
    const authenticatedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      }
    });

    return next(authenticatedRequest);
  }

  return next(req);
};