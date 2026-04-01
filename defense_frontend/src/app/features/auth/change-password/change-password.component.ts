import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '@core/services';

const passwordsMatchValidator: ValidatorFn = (control): ValidationErrors | null => {
  const newPassword = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;

  if (!newPassword || !confirmPassword) {
    return null;
  }

  return newPassword === confirmPassword ? null : { passwordsMismatch: true };
};

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  templateUrl: './change-password.component.html',
  styles: [`
    .login-container {
      min-height: 100vh;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #1e3a5f 0%, #2d5a87 100%);
      padding: 24px;
    }

    .login-card {
      width: 100%;
      max-width: 420px;
      padding: 32px;
    }

    mat-card-header {
      display: flex;
      flex-direction: column;
      align-items: center;
      margin-bottom: 24px;
    }

    .logo {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 28px;
      font-weight: 700;
      color: #1e3a5f;

      mat-icon {
        font-size: 36px;
        width: 36px;
        height: 36px;
        color: #2d5a87;
      }
    }

    mat-card-subtitle {
      text-align: center;
      margin-top: 8px;
      font-size: 14px;
    }

    mat-card-content {
      padding: 0;
    }

    .reset-notice {
      background: #e3f2fd;
      border: 1px solid #90caf9;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 24px;
    }

    .reset-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
      color: #1565c0;
      margin: 0 0 8px 0;
      font-size: 14px;
    }

    .reset-title mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .reset-subtitle {
      margin: 0;
      color: #2d5a87;
      font-size: 13px;
      line-height: 1.4;
    }

    .full-width {
      width: 100%;
    }

    mat-form-field {
      margin-bottom: 8px;
    }

    .actions {
      display: flex;
      justify-content: flex-end;
      margin-top: 16px;
    }

    .submit-button {
      width: 100%;
      height: 48px;
      font-size: 16px;
    }

    mat-progress-spinner {
      display: inline-block;
    }
  `]
})
export class ChangePasswordComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  isSubmitting = false;
  hideCurrentPassword = true;
  hideNewPassword = true;
  hideConfirmPassword = true;

  changePasswordForm = this.fb.group(
    {
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    },
    { validators: passwordsMatchValidator }
  );

  ngOnInit(): void {
    if (!this.authService.isAuthenticated()) {
      this.router.navigate(['/login']);
    }
  }

  onSubmit(): void {
    if (this.changePasswordForm.invalid) {
      this.changePasswordForm.markAllAsTouched();
      return;
    }

    this.isSubmitting = true;

    const { currentPassword, newPassword } = this.changePasswordForm.getRawValue();

    this.authService.changePassword({ currentPassword: currentPassword!, newPassword: newPassword! }).subscribe({
      next: () => {
        this.isSubmitting = false;
        this.snackBar.open('Mot de passe modifié avec succès', 'Fermer', { duration: 3000 });
        this.router.navigate(['/dashboard']);
      },
      error: (error) => {
        this.isSubmitting = false;
        this.snackBar.open(
          error?.error?.message || error?.message || 'Une erreur est survenue',
          'Fermer',
          { duration: 5000 }
        );
      }
    });
  }
}