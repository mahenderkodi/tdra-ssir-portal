import { inject } from '@angular/core';
import {
  CanActivateFn,
  Router
} from '@angular/router';

import { AuthService } from './auth-service';
import { LoggerService } from '../../layouts/logging/loggerService';

/**
 * Restricts routes based on the roles configured in app.routes.ts.
 */

//route = information/configuration about the route you are trying to enter.
//state = information about the complete navigation currently happening. state.url
export const roleGuard: CanActivateFn = (
  route,
  state
) => {

  const authService = inject(AuthService);
  const router = inject(Router);
  const logger = inject(LoggerService);

  // Prevent unauthenticated users from accessing role-protected routes.
  if (!authService.isAuthenticated()) {
    return router.createUrlTree(
      ['/auth/login'],
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

  // If no roles are specified, any authenticated user is allowed.
  if (allowedRoles.length === 0) {
    return true;
  }

  // Allow access when the user has at least one required role.
  if (authService.hasAnyRole(allowedRoles)) {
    return true;
  }

  logger.warn(
  'Route access denied because required role is missing'
);

  // Logged-in user does not have permission for this route.
  return router.createUrlTree([
    '/unauthorized'
  ]);
};