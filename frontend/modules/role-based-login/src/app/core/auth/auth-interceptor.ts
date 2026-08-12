import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { TokenStorageService } from './token-storage';

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

    const authenticatedRequest = req.clone({
      setHeaders: {
        Authorization: `Bearer ${accessToken}`
      }
    });

    return next(authenticatedRequest);
  }

  return next(req);
};