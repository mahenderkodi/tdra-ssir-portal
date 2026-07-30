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
  private authService = inject(AuthService);
  private router = inject(Router);

  errorMessage = signal('');
  isSubmitting = signal(false);

  readonly loginForm: FormGroup = this.fb.group({
    usernameOrEmail: ['', [Validators.required]],
    password: ['', [Validators.required, Validators.minLength(8)]]
  });

  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.isSubmitting.set(true);
    this.errorMessage.set('');

    this.authService.login(this.loginForm.value).subscribe({
      next: (response) => {
        this.isSubmitting.set(false);
        this.redirectUserByRole(response.roles);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        this.errorMessage.set(err.error?.message || 'Authentication failed. Please verify your credentials.');
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