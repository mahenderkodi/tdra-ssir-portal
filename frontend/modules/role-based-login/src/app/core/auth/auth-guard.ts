/*If an in-memory authenticated session exists, allow the protected 
route. Otherwise redirect to /auth/login while preserving the 
requested URL; guestGuard then decides whether this is a genuine guest 
or a restorable refresh-token session */

import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth-service';

// CanActivateFn means: Angular can execute this function before activating a route.
// route = information about the particular route.
// state = information about the complete navigation.
export const authGuard: CanActivateFn = (route, state) => {

  const authService = inject(AuthService);
  const router = inject(Router);

  

  // Block protected routes when no access token exists.
  if (!authService.isLoggedIn()) {

    router.createUrlTree(
      ['/auth/login'],
      {
        queryParams: {
          returnUrl: state.url
        }
      }
    );

    // return false;
  }

  return true;
};