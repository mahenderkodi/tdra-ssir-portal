import { Component, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth-service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule,RouterLink], // Registers form directives
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
     next: response => {
  console.log(
    'Login response:',
    response
  );

  console.log(
    'Roles received:',
    response.roles
  );

  console.log(
    'First-time login:',
    response.firstTimeLogin
  );

  this.isSubmitting.set(false);

  if (
    response.firstTimeLogin === true
  ) {
    void this.router.navigate([
      '/auth/create-password'
    ]);

    return;
  }

  if (
    response.firstTimeLogin === false
  ) {
    this.redirectUserByRole(
      response.roles
    );

    return;
  }

  this.errorMessage.set(
    'Login succeeded, but the account status was not provided.'
  );

  this.authService.logout();
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

 private redirectUserByRole(
  roles: string[]
): void {

  if (
    roles.includes(
      'ROLE_COMPANY_PENDING'
    )
  ) {
    void this.router.navigate([
      '/portal/track-status'
    ]);

    return;
  }


  if (
    roles.includes(
      'ROLE_COMPANY_ADMIN'
    )
  ) {
    void this.router.navigate([
      '/portal/dashboard'
    ]);

    return;
  }


  void this.router.navigate([
    '/unauthorized'
  ]);
}

  loginWithUaePass(): void {
    window.location.href = 'https://id.uaepass.ae/idp/v1/user/authorize?response_type=code&client_id=...';
  }
}