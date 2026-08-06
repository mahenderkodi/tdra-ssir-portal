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


export const guestGuard:
  CanActivateFn = () => {

  const authService =
    inject(AuthService);

  const tokenStorage =
    inject(TokenStorageService);

  const router =
    inject(Router);


  /*
   * This function redirects an
   * authenticated user by role.
   */
  const redirectByRole = () => {

    const roles =
      authService.getCurrentRoles();


    const isTdraUser =
      roles.some(role =>
        TDRA_ROLES.includes(role)
      );


    if (isTdraUser) {
      return router.createUrlTree([
        '/admin/dashboard'
      ]);
    }


    if (
      roles.includes(
        'ROLE_COMPANY_PENDING'
      )
    ) {
      return router.createUrlTree([
        '/portal/track-status'
      ]);
    }


    if (
      roles.includes(
        'ROLE_COMPANY_ADMIN'
      )
    ) {
      return router.createUrlTree([
        '/portal/dashboard'
      ]);
    }


    return router.createUrlTree([
      '/unauthorized'
    ]);
  };


  /*
   * Access token already exists.
   */
  if (authService.isAuthenticated()) {
    return redirectByRole();
  }


  /*
   * No access token and no refresh token:
   * allow the login page.
   */
  if (!tokenStorage.hasRefreshToken()) {
    return true;
  }


  /*
   * Refresh token exists.
   *
   * Wait for the refresh API before
   * deciding whether login can open.
   */
  return authService
    .refreshToken()
    .pipe(
      map(() =>
        redirectByRole()
      ),

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