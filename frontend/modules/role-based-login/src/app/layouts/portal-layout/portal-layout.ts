import { Component, inject } from '@angular/core';
import { PortalDashboard } from "../../pages/portal/dashboard/dashboard";
import { RouterModule } from '@angular/router';
 import {
  Router
} from '@angular/router';

import {
  AuthService
} from '../../core/auth/auth-service';

@Component({
  selector: 'app-portal-layout',
  imports: [PortalDashboard,RouterModule],
  templateUrl: './portal-layout.html',
  styleUrl: './portal-layout.css',
})
export class PortalLayout {
  private readonly authService =
    inject(AuthService);

  private readonly router =
    inject(Router);
  logout(): void {
    this.authService.logout();

    void this.router.navigate([
      '/auth/login'
    ]);
  }
}
