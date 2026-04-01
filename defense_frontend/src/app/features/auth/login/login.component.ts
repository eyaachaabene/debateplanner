import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { AuthService } from '@core/services';
import { environment } from '@environments/environment';

interface DemoCredential {
  role: string;
  username: string;
  password: string;
}

@Component({
  selector: 'app-login',
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
    MatProgressSpinnerModule,
    MatChipsModule,
    MatTooltipModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <mat-card-title>
            <div class="logo">
              <mat-icon>school</mat-icon>
              <span>ENSAJ</span>
            </div>
          </mat-card-title>
          <mat-card-subtitle>Gestion des Soutenances de PFE</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          @if (useMocks) {
            <div class="demo-credentials">
              <p class="demo-title">
                <mat-icon>info</mat-icon>
                Mode démo - Comptes de test
              </p>
              <div class="credentials-list">
                @for (cred of demoCredentials; track cred.username) {
                  <button
                    mat-stroked-button
                    class="credential-chip"
                    type="button"
                    (click)="fillCredentials(cred)"
                    [matTooltip]="'Nom d\\'utilisateur : ' + cred.username"
                  >
                    <mat-icon>{{ getRoleIcon(cred.role) }}</mat-icon>
                    {{ cred.role }}
                  </button>
                }
              </div>
            </div>
          }

          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Nom d'utilisateur</mat-label>
              <input
                matInput
                formControlName="username"
                type="text"
                placeholder="Votre nom d'utilisateur"
              />
              <mat-icon matSuffix>person</mat-icon>

              @if (loginForm.get('username')?.hasError('required') && loginForm.get('username')?.touched) {
                <mat-error>Le nom d'utilisateur est requis</mat-error>
              }
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Mot de passe</mat-label>
              <input
                matInput
                formControlName="password"
                [type]="hidePassword ? 'password' : 'text'"
              />
              <button
                mat-icon-button
                matSuffix
                type="button"
                (click)="hidePassword = !hidePassword"
              >
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>

              @if (loginForm.get('password')?.hasError('required') && loginForm.get('password')?.touched) {
                <mat-error>Le mot de passe est requis</mat-error>
              }
            </mat-form-field>

            <button
              mat-flat-button
              color="primary"
              class="full-width login-button"
              type="submit"
              [disabled]="loginForm.invalid || isLoading"
            >
              @if (isLoading) {
                <mat-spinner diameter="20"></mat-spinner>
              } @else {
                Se connecter
              }
            </button>
          </form>
        </mat-card-content>

        @if (useMocks) {
          <mat-card-footer class="mock-notice">
            <mat-icon>warning</mat-icon>
            <span>Données de démonstration - Aucune connexion backend requise</span>
          </mat-card-footer>
        }
      </mat-card>
    </div>
  `,
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

    .demo-credentials {
      background: #e3f2fd;
      border: 1px solid #90caf9;
      border-radius: 8px;
      padding: 16px;
      margin-bottom: 24px;
    }

    .demo-title {
      display: flex;
      align-items: center;
      gap: 8px;
      font-weight: 500;
      color: #1565c0;
      margin: 0 0 12px 0;
      font-size: 14px;

      mat-icon {
        font-size: 18px;
        width: 18px;
        height: 18px;
      }
    }

    .credentials-list {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .credential-chip {
      font-size: 12px;

      mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
        margin-right: 4px;
      }
    }

    .full-width {
      width: 100%;
    }

    mat-form-field {
      margin-bottom: 8px;
    }

    .login-button {
      height: 48px;
      font-size: 16px;
      margin-top: 16px;
    }

    mat-spinner {
      display: inline-block;
    }

    .mock-notice {
      display: flex;
      align-items: center;
      gap: 8px;
      justify-content: center;
      padding: 12px;
      background: #fff3e0;
      color: #e65100;
      font-size: 12px;
      border-radius: 0 0 4px 4px;
      margin: 24px -32px -32px -32px;

      mat-icon {
        font-size: 16px;
        width: 16px;
        height: 16px;
      }
    }
  `]
})
export class LoginComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  loginForm: FormGroup = this.fb.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  hidePassword = true;
  isLoading = false;
  useMocks = environment.useMocks;

  demoCredentials: DemoCredential[] = [
    { role: 'Admin', username: 'admin', password: 'admin123' },
    { role: 'Professeur', username: 'prof.martin', password: 'prof123' },
    { role: 'Étudiant', username: 'etudiant.leroy', password: 'student123' }
  ];

  ngOnInit(): void {
    if (this.useMocks) {
      this.fillCredentials(this.demoCredentials[0]);
    }
  }

  fillCredentials(cred: DemoCredential): void {
    this.loginForm.patchValue({
      username: cred.username,
      password: cred.password
    });
  }

  getRoleIcon(role: string): string {
    switch (role) {
      case 'Admin':
        return 'admin_panel_settings';
      case 'Professeur':
        return 'person';
      case 'Étudiant':
        return 'school';
      default:
        return 'person';
    }
  }

  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;

    this.authService.login(this.loginForm.getRawValue()).subscribe({
      next: (response) => {
        this.isLoading = false;

        if (!response.mustChangePassword) {
          this.router.navigate(['/dashboard']);
        }

        this.snackBar.open(
          'Connexion réussie',
          'Fermer',
          { duration: 3000 }
        );
      },
      error: (error) => {
        this.isLoading = false;

        this.snackBar.open(
          error?.error?.message || error?.message || 'Identifiants incorrects',
          'Fermer',
          {
            duration: 5000,
            panelClass: ['error-snackbar']
          }
        );
      }
    });
  }
}