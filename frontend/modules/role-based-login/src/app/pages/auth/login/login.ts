import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common'; //common angular functionality like NgIf available to standalone component
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth-service';
import { LoggerService } from '../../../layouts/logging/loggerService';

//ReactiveFormsModule allows the template to use [formGroup], formControlName
@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink], // Registers form directives
  templateUrl: './login.html',
  styleUrl: './login.css' // Mapped to login.css
})
export class Login {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly logger = inject(LoggerService);

  errorMessage = signal('');
  isSubmitting = signal(false);

  readonly loginForm: FormGroup = this.fb.group({
    usernameOrEmail: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });




  onSubmit(): void {

    if (
      this.authService.isAuthenticated()
    ) {
      const roles =
        this.authService.getCurrentRoles();

      const user =
        this.authService.currentUser();

      this.redirectUserByRole(roles, user?.companyId ?? null);

      return;
    }
    
    this.errorMessage.set('');

    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    const credentials =
      this.loginForm.getRawValue();

    this.authService
      .login(credentials)
      .subscribe({
        next: response => {


          this.isSubmitting.set(false);

          if (
            response.firstTimeLogin === true
          ) {
            this.logger.info(
              'Login successful; password setup required'
            );
            void this.router.navigate([
              '/auth/create-password'
            ]);

            return;
          }

          if (
            response.firstTimeLogin === false
          ) {
            this.redirectUserByRole(
              response.roles,
              response.companyId
            );
            this.logger.info(
              'Login successful'
            );
            return;
          }

          this.logger.error(
            'Login response missing account status'
          );

          this.errorMessage.set(
            'Login succeeded, but the account status was not provided.'
          );

          this.authService.logout();
        },

        error: () => {
          this.logger.warn(
            'Login attempt failed'
          );

          this.isSubmitting.set(false);

          this.errorMessage.set(
            'Invalid username/email or password.'
          );
        }
      });
  }

  private redirectUserByRole(
    roles: string[],
    companyId: number | null
  ): void {

    const isTdraUser =
      roles.some(role =>
        [
          'ROLE_TDRA_SUPER_ADMIN',
          'ROLE_TDRA_REVIEWER',
          'ROLE_TDRA_APPROVER',
          'ROLE_TDRA_AUDITOR'
        ].includes(role)
      );

    if (isTdraUser) {
      void this.router.navigate([
        '/admin/dashboard'
      ]);
      return;
    }

    const isCompanyUser =
      roles.includes('ROLE_COMPANY_PENDING') ||
      roles.includes('ROLE_COMPANY_ADMIN');

    if (isCompanyUser) {

      if (companyId === null) {

        void this.router.navigate([
          '/portal/sender-id/new'
        ]);

        return;
      }

      void this.router.navigate([
        '/portal/dashboard'
      ]);

      return;
    }
    this.logger.warn(
      'Authenticated user has no recognized application role'
    );
    void this.router.navigate([
      '/unauthorized'
    ]);
  }

  loginWithUaePass(): void {
    window.location.href = 'https://id.uaepass.ae/idp/v1/user/authorize?response_type=code&client_id=...';
  }
}