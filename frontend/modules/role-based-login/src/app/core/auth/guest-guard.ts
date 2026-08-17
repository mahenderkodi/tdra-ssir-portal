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


export const guestGuard: CanActivateFn =
  (route) => {

    const authService =
      inject(AuthService);

    const tokenStorage =
      inject(TokenStorageService);

    const router =
      inject(Router);


    /*
     * Original page that the user was
     * trying to access before auth/session
     * restoration.
     *
     * Example:
     * /portal/sender-id/3
     */
    const returnUrl =
      route.queryParamMap.get(
        'returnUrl'
      );


    const redirectByRole = () => {

      const roles =
        authService.getCurrentRoles();

      const user =
        authService.currentUser();


      const isTdraUser =
        roles.some(
          role =>
            TDRA_ROLES.includes(role)
        );


      /*
       * TDRA user
       */
      if (isTdraUser) {

        /*
         * If TDRA was already on an admin
         * page before refresh, restore it.
         */
        if (
          returnUrl &&
          (
            returnUrl === '/admin' ||
            returnUrl.startsWith('/admin/')
          )
        ) {

          return router.parseUrl(
            returnUrl
          );
        }


        return router.createUrlTree([
          '/admin/dashboard'
        ]);
      }


      const isCompanyUser =
        roles.includes(
          'ROLE_COMPANY_PENDING'
        ) ||
        roles.includes(
          'ROLE_COMPANY_ADMIN'
        );


      /*
       * Company user
       */
      if (isCompanyUser) {

        /*
         * Important:
         * Restore the exact portal page
         * that was open before refresh.
         *
         * Example:
         * /portal/sender-id/3
         */
        if (
          returnUrl &&
          (
            returnUrl === '/portal' ||
            returnUrl.startsWith('/portal/')
          )
        ) {

          return router.parseUrl(
            returnUrl
          );
        }


        /*
         * New company with no company yet.
         */
        if (
          user?.companyId == null
        ) {

          return router.createUrlTree([
            '/portal/sender-id/new'
          ]);
        }


        /*
         * Normal login landing page.
         */
        return router.createUrlTree([
          '/portal/dashboard'
        ]);
      }


      return router.createUrlTree([
        '/unauthorized'
      ]);
    };


    /*
     * Already authenticated.
     */
    if (
      authService.isAuthenticated()
    ) {

      return redirectByRole();
    }


    /*
     * Genuine guest.
     */
    if (
      !tokenStorage.hasRefreshToken()
    ) {

      return true;
    }


    /*
     * Browser refresh:
     * restore session first.
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