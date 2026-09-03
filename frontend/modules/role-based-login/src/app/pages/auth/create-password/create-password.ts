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
import { passwordPolicyValidator } from '../../../core/validators/password-policy.validator';
import { LoggerService } from '../../../layouts/logging/loggerService';
import { TranslatePipe } from '@ngx-translate/core';
@Component({
  selector: 'app-create-password',
  standalone: true,
  imports: [
  ReactiveFormsModule,
  TranslatePipe
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

  private readonly logger = inject(LoggerService);

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
            Validators.minLength(6),
            Validators.maxLength(15),

            passwordPolicyValidator(
              () =>
                this.authService
                  .currentUser()
                  ?.username ?? ''
            )
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
        next: () => {

  this.logger.info(
    'Permanent password set successfully'
  );

  this.isSubmitting.set(false);
  this.passwordCreated.set(true);

  this.successMessage.set(
    'auth.createPassword.success'
  );
},

        error: (error) => {
          this.isSubmitting.set(false);

          if (error.status === 401) {
            this.logger.warn(
              'Temporary login session expired during password setup'
            );

            this.errorMessage.set(
  'errors.CREATEPWD001'
);

            return;
          }

          if (error.status === 403) {
            this.logger.warn(
              'Password setup denied by authorization'
            );

            this.errorMessage.set(
  'errors.CREATEPWD002'
);

            return;
          }

          this.logger.error(
            'Password setup failed'
          );
          this.errorMessage.set(
    'errors.CREATEPWD003'
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