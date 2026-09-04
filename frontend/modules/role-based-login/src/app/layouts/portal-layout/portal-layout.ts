import {
  Component,
  inject
} from '@angular/core';

import {
  RouterModule,
  Router
} from '@angular/router';

import {
  TranslatePipe
} from '@ngx-translate/core';

import {
  PortalDashboard
} from '../../pages/portal/dashboard/dashboard';

import {
  AuthService
} from '../../core/auth/auth-service';

@Component({
  selector: 'app-portal-layout',

  imports: [
    PortalDashboard,
    RouterModule,
    TranslatePipe
  ],

  templateUrl: './portal-layout.html',
  styleUrl: './portal-layout.css',
})
export class PortalLayout {

  private readonly authService =
    inject(AuthService);

  private readonly router =
    inject(Router);

  mobileMenuOpen = false;


  toggleMobileMenu(): void {

    this.mobileMenuOpen =
      !this.mobileMenuOpen;
  }


  logout(): void {

    this.authService.logout();

    void this.router.navigate([
      '/auth/login'
    ]);
  }
}