import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  TranslatePipe
} from '@ngx-translate/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  RouterModule
} from '@angular/router';

import {
  AuthService
} from '../../../core/auth/auth-service';

import {
  LoggerService
} from '../../../layouts/logging/loggerService';

@Component({
  selector: 'app-forgot-password',
  imports: [
  ReactiveFormsModule,
  RouterModule,
  TranslatePipe
],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {

  private readonly fb =
    inject(FormBuilder);

  private readonly authService =
    inject(AuthService);

  private readonly logger =
    inject(LoggerService);

  readonly forgotForm =
    this.fb.nonNullable.group({
      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ]
    });

  readonly successMessage =
    signal('');

  readonly errorMessage =
    signal('');

  readonly isSubmitting =
    signal(false);

  onSubmit(): void {

    if (this.forgotForm.invalid) {

      this.forgotForm
        .markAllAsTouched();

      return;
    }

    this.isSubmitting.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    const email =
      this.forgotForm.getRawValue().email;

    this.authService
      .forgotPassword(email)
      .subscribe({

  next: () => {

    this.logger.info(
      'Password reset link requested'
    );

    this.isSubmitting.set(false);

    this.successMessage.set(
      'auth.forgotPassword.success'
    );

    this.forgotForm.reset();
  },

  error: () => {

    this.logger.warn(
      'Password reset link request failed'
    );

    this.isSubmitting.set(false);

    this.errorMessage.set(
      'errors.FORGOT001'
    );
  }
});
  }
}