import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule], // Registers form directives
  templateUrl: './login.html',
  styleUrl: './login.css' // Mapped to login.css
})
export class Login {
  private fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  errorMessage = signal('');
  isSubmitting = signal(false);

  readonly loginForm: FormGroup = this.fb.group({
    usernameOrEmail: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  onSubmit(): void {
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
      next: (response) => {
  console.log(
    'Login response:',
    response
  );

  this.isSubmitting.set(false);

  if (response.firstTimeLogin) {
    void this.router.navigate([
      '/create-password'
    ]);

    return;
  }

  this.redirectUserByRole(response.roles);
},

      error: (error) => {
        console.error(
          'Login error:',
          error
        );

        this.isSubmitting.set(false);

        this.errorMessage.set(
          'Invalid username/email or password.'
        );
      }
    });
}

  private redirectUserByRole(roles: string[]): void {
    if (roles.includes('ROLE_TDRA_SUPER_ADMIN') || roles.includes('ROLE_TDRA_REVIEWER') || roles.includes('ROLE_TDRA_APPROVER')) {
      this.router.navigate(['/admin/dashboard']);
    } else if (roles.includes('ROLE_COMPANY_ADMIN') || roles.includes('ROLE_COMPANY_USER')) {
      this.router.navigate(['/portal/dashboard']);
    } else {
      this.router.navigate(['/auth/login']);
    }
  }

  loginWithUaePass(): void {
    window.location.href = 'https://id.uaepass.ae/idp/v1/user/authorize?response_type=code&client_id=...';
  }
}