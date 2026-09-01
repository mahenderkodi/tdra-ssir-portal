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

import {
  passwordPolicyValidator
} from '../../../core/validators/password-policy.validator';


import { LoggerService } from '../../../layouts/logging/loggerService';

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

  private readonly logger = inject(LoggerService);


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
          Validators.minLength(6),
          Validators.maxLength(15),

          passwordPolicyValidator(
            () => this.getUsername()
          )
        ]
      ]
    });

  constructor() {

    this.signupForm.controls.username
      .valueChanges
      .subscribe(() => {

        this.signupForm.controls.password
          .updateValueAndValidity({
            emitEvent: false
          });
      });
  }

  private getUsername(): string {

    return this.signupForm
      ?.controls
      .username
      .value ?? '';
  }


  onSubmit(): void {

    this.errorMessage.set('');


    if (this.signupForm.invalid) {

      this.signupForm
        .markAllAsTouched();

      return;
    }


    this.isSubmitting.set(true);


    this.authService
      .registerInit(
        this.signupForm.getRawValue()
      )
      .subscribe({

        next: response => {

          this.logger.info(
            'Account registration initialized successfully'
          );
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

          if (error.status >= 500) {

            this.logger.error(
              'Account registration failed due to server error'
            );

            this.errorMessage.set(
              'Unable to create account. Please try again later.'
            );

          } else {

            this.logger.warn(
              'Account registration request rejected'
            );

            this.errorMessage.set(
              error.error?.message ??
              'Unable to create account.'
            );
          }

          this.isSubmitting.set(false);
        }

      });
  }
}