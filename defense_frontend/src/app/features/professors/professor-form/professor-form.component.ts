import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ProfessorService } from '@core/services';
import { LoadingSpinnerComponent } from '@shared/components';

@Component({
  selector: 'app-professor-form',
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
    LoadingSpinnerComponent
  ],
  template: `
    <div class="professor-form">
      <div class="page-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h1>{{ isEditMode ? 'Modifier le professeur' : 'Ajouter un professeur' }}</h1>
      </div>

      @if (isLoadingProfessor()) {
        <app-loading-spinner message="Chargement..."></app-loading-spinner>
      } @else {
        <mat-card>
          <mat-card-content>
            <form [formGroup]="professorForm" (ngSubmit)="onSubmit()">
              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Prénom</mat-label>
                  <input matInput formControlName="firstName" />
                  @if (professorForm.get('firstName')?.hasError('required')) {
                    <mat-error>Le prénom est requis</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Nom</mat-label>
                  <input matInput formControlName="lastName" />
                  @if (professorForm.get('lastName')?.hasError('required')) {
                    <mat-error>Le nom est requis</mat-error>
                  }
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Email</mat-label>
                  <input matInput formControlName="email" type="email" />
                  @if (professorForm.get('email')?.hasError('required')) {
                    <mat-error>L'email est requis</mat-error>
                  }
                  @if (professorForm.get('email')?.hasError('email')) {
                    <mat-error>Format d'email invalide</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>User ID (optionnel)</mat-label>
                  <input matInput formControlName="userId" type="number" min="1" />
                </mat-form-field>
              </div>

              <div class="actions">
                <button mat-button type="button" (click)="goBack()">Annuler</button>
                <button
                  mat-flat-button
                  color="primary"
                  type="submit"
                  [disabled]="professorForm.invalid || isSubmitting()"
                >
                  @if (isSubmitting()) {
                    <mat-spinner diameter="20"></mat-spinner>
                  } @else {
                    {{ isEditMode ? 'Mettre à jour' : 'Créer' }}
                  }
                </button>
              </div>
            </form>
          </mat-card-content>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .professor-form {
      max-width: 800px;
      margin: 0 auto;
    }

    .page-header {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;
    }

    .page-header h1 {
      font-size: 24px;
      font-weight: 500;
      margin: 0;
    }

    .form-row {
      display: flex;
      gap: 16px;
    }

    .form-row mat-form-field {
      flex: 1;
    }

    mat-form-field {
      margin-bottom: 8px;
    }

    .actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      margin-top: 16px;
    }
  `]
})
export class ProfessorFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly professorService = inject(ProfessorService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  professorForm: FormGroup = this.fb.group({
    userId: [null],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]]
  });

  isEditMode = false;
  professorId: number | null = null;
  isLoadingProfessor = signal(false);
  isSubmitting = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.isEditMode = true;
      this.professorId = +id;
      this.loadProfessor();
    }
  }

  private loadProfessor(): void {
    if (!this.professorId) return;

    this.isLoadingProfessor.set(true);

    this.professorService.getById(this.professorId).subscribe({
      next: (professor) => {
        this.professorForm.patchValue({
          userId: professor.userId ?? null,
          firstName: professor.firstName,
          lastName: professor.lastName,
          email: professor.email
        });
        this.isLoadingProfessor.set(false);
      },
      error: () => {
        this.isLoadingProfessor.set(false);
        this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 5000 });
        this.goBack();
      }
    });
  }

  onSubmit(): void {
    if (this.professorForm.invalid) {
      this.professorForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    const formData = this.professorForm.getRawValue();
    
    // ✅ ADD THIS CONSOLE LOG
    console.log('Sending professor data:', formData);
    console.log('Professor data JSON:', JSON.stringify(formData));

    const request$ = this.isEditMode
      ? this.professorService.update(this.professorId!, formData)
      : this.professorService.create(formData);

    request$.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        const successMessage = this.isEditMode
          ? 'Professeur mis à jour'
          : `Professeur créé avec succès. Identifiants : username = ${formData.email}, mot de passe temporaire = ChangeMe123! — Ils devront le modifier à la première connexion.`;
        this.snackBar.open(
          successMessage,
          'Fermer',
          { duration: this.isEditMode ? 3000 : 8000 }
        );
        this.goBack();
      },
      error: (error) => {
        console.error('Professor creation error:', error);
        this.isSubmitting.set(false);
        this.snackBar.open(
          error?.error?.message || 'Une erreur est survenue',
          'Fermer',
          { duration: 5000 }
        );
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard/professors']);
  }
}