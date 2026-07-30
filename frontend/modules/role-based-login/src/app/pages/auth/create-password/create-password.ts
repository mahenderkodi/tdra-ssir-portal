import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators
} from '@angular/forms';

import {
  Router
} from '@angular/router';

import {
  AuthService
} from '../../../core/auth/auth-service';

@Component({
  selector: 'app-create-password',
  standalone: true,
  imports: [
    ReactiveFormsModule
  ],
  templateUrl: './create-password.html',
  styleUrl: './create-password.css'
})
export class CreatePassword {
  private readonly router =
    inject(Router);

  private readonly formBuilder =
    inject(FormBuilder);

  private readonly authService =
    inject(AuthService);

  readonly successMessage = signal('');
  readonly errorMessage = signal('');
  readonly isSubmitting = signal(false);
  readonly passwordCreated = signal(false);

  readonly passwordForm =
    this.formBuilder.nonNullable.group(
      {
        password: [
          '',
          [
            Validators.required,
            Validators.minLength(8)
          ]
        ],

        confirmPassword: [
          '',
          Validators.required
        ]
      },
      {
        validators:
          this.passwordMatchValidator
      }
    );

  private passwordMatchValidator(
    control: AbstractControl
  ): ValidationErrors | null {
    const password =
      control.get('password')?.value;

    const confirmPassword =
      control.get('confirmPassword')?.value;

    return password === confirmPassword
      ? null
      : { mismatch: true };
  }

  onSubmit(): void {
    this.successMessage.set('');
    this.errorMessage.set('');

    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      return;
    }

    const formValue =
      this.passwordForm.getRawValue();

    this.isSubmitting.set(true);

    this.authService
      .setupPassword({
        password: formValue.password
      })
      .subscribe({
        next: (response) => {
          console.log(
            'Setup-password response:',
            response
          );

          this.isSubmitting.set(false);
          this.passwordCreated.set(true);

          this.successMessage.set(
            response.message
          );
        },

        error: (error) => {
          console.error(
            'Setup-password error:',
            error
          );

          this.isSubmitting.set(false);

          if (error.status === 401) {
            this.errorMessage.set(
              'Your temporary login session has expired. Please sign in again.'
            );

            return;
          }

          if (error.status === 403) {
            this.errorMessage.set(
              'You are not authorized to set a permanent password.'
            );

            return;
          }

          this.errorMessage.set(
            error.error?.message ??
            'Password update failed.'
          );
        }
      });
  }

  goToLogin(): void {
    /*
     * Remove the temporary login session.
     */
    this.authService.logout();

    void this.router.navigate([
      '/auth/login'
    ]);
  }
}