import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth-service';
import { signal } from '@angular/core';


@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-layout.html', // Pointing to HTML [3]
  styleUrl: './admin-layout.css'     // Pointing to CSS [3]
})
export class AdminLayoutComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  readonly sidebarOpen =
  signal(false);
  readonly currentUser = this.authService.currentUser;

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/auth/login']);
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