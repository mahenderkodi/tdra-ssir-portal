import { inject } from '@angular/core';

import {
  CanActivateFn,
  Router
} from '@angular/router';

import {
  catchError,
  map,
  of
} from 'rxjs';

import { AuthService } from './auth-service';
import { TokenStorageService } from './token-storage';


const TDRA_ROLES: string[] = [
  'ROLE_TDRA_SUPER_ADMIN',
  'ROLE_TDRA_REVIEWER',
  'ROLE_TDRA_APPROVER',
  'ROLE_TDRA_AUDITOR'
];


export const guestGuard: CanActivateFn = () => {

  const authService = inject(AuthService);
  const tokenStorage = inject(TokenStorageService);
  const router = inject(Router);


  // Redirects an already logged-in user to the correct portal based on role.
  const redirectByRole = () => {

    const roles = authService.getCurrentRoles();
    const user = authService.currentUser();

    const isTdraUser =
      roles.some(role =>
        TDRA_ROLES.includes(role)
      );

    if (isTdraUser) {
      return router.createUrlTree([
        '/admin/dashboard'
      ]);
    }


    const isCompanyUser =
      roles.includes('ROLE_COMPANY_PENDING') ||
      roles.includes('ROLE_COMPANY_ADMIN');

    if (isCompanyUser) {

      // Current logic uses companyId to decide between initial form and dashboard.
      if (user?.companyId == null) {
        return router.createUrlTree([
          '/portal/sender-id/new'
        ]);
      }

      return router.createUrlTree([
        '/portal/dashboard'
      ]);
    }

    return router.createUrlTree([
      '/unauthorized'
    ]);
  };


  // If access token already exists, do not allow login/signup page.
  if (authService.isAuthenticated()) {
    return redirectByRole();
  }


  // No active session exists, so guest can access login/signup.
  if (!tokenStorage.hasRefreshToken()) {
    return true;
  }


  // Restore the session from refresh token before deciding where to redirect.
  return authService
    .refreshToken()
    .pipe(

      map(() =>
        redirectByRole()
      ),

      // Invalid/expired refresh token means treat the user as logged out.
      catchError(error => {

        console.error(
          'Unable to restore session:',
          error
        );

        authService.logout();

        return of(true);
      })
    );
};