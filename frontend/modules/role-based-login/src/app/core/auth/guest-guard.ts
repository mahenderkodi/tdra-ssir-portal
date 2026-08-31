/* guestGuard prevents an already authenticated/restorable 
user from seeing guest pages like Login. If the access token 
is gone but a refresh token still exists, it restores the session 
using the refresh token and redirects the user back to the appropriate page. If no refresh token exists, it allows the Login page to open. */

import { inject } from '@angular/core';

// Angular type for a functional route guard - CanActivateFn
// Router - lets guard construct redirects
import {
  CanActivateFn,
  Router
} from '@angular/router';

//map() -> transform successful emittedvalue
//catchError() -> handle an observable failure
//of() -> create an observable that immediately emits a value
import {
  catchError,
  map,
  of
} from 'rxjs';

import { AuthService } from './auth-service';
import { TokenStorageService } from './token-storage';
import { LoggerService } from '../../layouts/logging/loggerService';


const TDRA_ROLES: string[] = [
  'ROLE_TDRA_SUPER_ADMIN',
  'ROLE_TDRA_REVIEWER',
  'ROLE_TDRA_APPROVER',
  'ROLE_TDRA_AUDITOR'
];

//authGuard does nothing using route, but authguard uses 
// route to read query parameters from it
export const guestGuard: CanActivateFn =
  (route) => {

    const authService =
      inject(AuthService);

    const tokenStorage =
      inject(TokenStorageService);

    const router =
      inject(Router);

    const logger =
      inject(LoggerService);
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

        catchError(() => {

          logger.error(
            'Unable to restore authentication session'
          );

          authService.logout();

          return of(true);
        })

      );
  };