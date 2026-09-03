import { Component, OnInit, signal, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/auth/auth-service';
import { LoggerService } from '../../../layouts/logging/loggerService';
import { TranslatePipe } from '@ngx-translate/core';

@Component({
  selector: 'app-reset-password',
  imports: [CommonModule, ReactiveFormsModule,TranslatePipe],

  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private logger = inject(LoggerService);

  resetForm!: FormGroup;
  token: string = '';
  successMessage = signal('');
  errorMessage = signal('');
  isSubmitting = signal(false);

  ngOnInit(): void {
    // Extract the token parameter from the secure email link query string [1]
    this.token = this.route.snapshot.queryParamMap.get('token') || '';

    if (!this.token) {
      this.logger.warn(
        'Password reset link is missing a token'
      );
     this.errorMessage.set(
  'errors.RESET001'
);
    }

    this.resetForm = this.fb.group({
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    const password = g.get('password')?.value;
    const confirm = g.get('confirmPassword')?.value;
    return password === confirm ? null : { mismatch: true };
  }

  onSubmit(): void {
    if (this.resetForm.invalid || !this.token) {
      return;
    }

    this.isSubmitting.set(true);
    this.successMessage.set('');
    this.errorMessage.set('');

    const payload = {
      token: this.token,
      newPassword: this.resetForm.value.password
    };

    this.authService.resetPassword(payload).subscribe({
      next: () => {

  this.logger.info(
    'Password reset completed successfully'
  );

  this.isSubmitting.set(false);

  this.successMessage.set(
    'auth.resetPassword.success'
  );

  setTimeout(
    () => this.router.navigate(['/auth/login']),
    3000
  );
},
      error: (err) => {
        this.logger.warn(
          'Password reset request rejected'
        );
        this.isSubmitting.set(false);
        this.errorMessage.set(
  'errors.RESET002'
);
      }
    });
  }
}