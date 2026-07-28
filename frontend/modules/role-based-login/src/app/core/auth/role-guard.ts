import { inject } from '@angular/core';
import {
  CanActivateFn,
  Router
} from '@angular/router';

import { AuthService } from './auth-service';

/**
 * Checks the allowed roles configured in app.routes.ts.
 */
export const roleGuard: CanActivateFn = (
  route,
  state
) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  /*
   * This makes the guard safe even when it is
   * accidentally used without authGuard.
   */
  if (!authService.isAuthenticated()) {
    return router.createUrlTree(
      ['/login'],
      {
        queryParams: {
          returnUrl: state.url
        }
      }
    );
  }

  const allowedRoles =
    (route.data['roles'] as string[] | undefined)
    ?? [];

  /*
   * No roles configured means that any authenticated
   * user can open the route.
   */
  if (allowedRoles.length === 0) {
    return true;
  }

  if (
    authService.hasAnyRole(allowedRoles)
  ) {
    return true;
  }

  return router.createUrlTree([
    '/unauthorized'
  ]);
};