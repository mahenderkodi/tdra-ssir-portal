import {
  Component,
  inject,
  signal
} from '@angular/core';

import {
  FormBuilder,
  ReactiveFormsModule,
  Validators
} from '@angular/forms';

import {
  Router,
  RouterLink
} from '@angular/router';

import {
  AuthService
} from '../../../core/auth/auth-service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    RouterLink
  ],
  templateUrl: './signup.html',
  styleUrl: './signup.css'
})
export class Signup {

  private readonly fb =
    inject(FormBuilder);

  private readonly authService =
    inject(AuthService);

  private readonly router =
    inject(Router);

  readonly errorMessage =
    signal('');

  readonly isSubmitting =
    signal(false);

  readonly signupForm =
    this.fb.nonNullable.group({

      email: [
        '',
        [
          Validators.required,
          Validators.email
        ]
      ],

      username: [
        '',
        Validators.required
      ],

      password: [
        '',
        [
          Validators.required,
          Validators.minLength(8)
        ]
      ]
    });


  onSubmit(): void {

    this.errorMessage.set('');

    if (this.signupForm.invalid) {
      this.signupForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    this.authService
      .registerInit(
        this.signupForm.getRawValue()
      )
      .subscribe({

        next: response => {

          this.isSubmitting.set(false);

          /*
           * Newly registered company users
           * normally have companyId = null.
           */
          if (response.companyId == null) {

            void this.router.navigate([
              '/portal/sender-id/new'
            ]);

            return;
          }

          void this.router.navigate([
            '/portal/dashboard'
          ]);
        },

        error: error => {

          console.error(
            'Signup error:',
            error
          );

          this.isSubmitting.set(false);

          this.errorMessage.set(
            error.error?.message ??
            'Unable to create account.'
          );
        }
      });
  }
}