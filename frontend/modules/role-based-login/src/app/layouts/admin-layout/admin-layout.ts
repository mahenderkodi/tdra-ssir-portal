import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  CommonModule
} from '@angular/common';

import {
  RouterModule,
  Router
} from '@angular/router';

import {
  TranslatePipe
} from '@ngx-translate/core';

import {
  AuthService
} from '../../core/auth/auth-service';


@Component({
  selector: 'app-admin-layout',

  standalone: true,

  imports: [
    CommonModule,
    RouterModule,
    TranslatePipe
  ],

  templateUrl: './admin-layout.html',
  styleUrl: './admin-layout.css'
})
export class AdminLayoutComponent {

  private readonly authService =
    inject(AuthService);

  private readonly router =
    inject(Router);

  readonly sidebarOpen =
    signal(false);


  logout(): void {

    this.authService.logout();

    void this.router.navigate([
      '/auth/login'
    ]);
  }


  toggleSidebar(): void {

    this.sidebarOpen.update(
      open => !open
    );
  }


  closeSidebar(): void {

    this.sidebarOpen.set(false);
  }
}
