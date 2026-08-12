import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth-service';

export const authGuard: CanActivateFn = (route, state) => {

  const authService = inject(AuthService);
  const router = inject(Router);

  // Block protected routes when no access token exists.
  if (!authService.isLoggedIn()) {

    router.navigate(
      ['/auth/login'],
      {
        queryParams: {
          returnUrl: state.url
        }
      }
    );

    return false;
  }

  return true;
};