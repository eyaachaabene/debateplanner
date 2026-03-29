import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { StudentService } from '@core/services';
import { Major } from '@core/models';
import { MajorLabelPipe } from '@shared/pipes';
import { LoadingSpinnerComponent } from '@shared/components';

@Component({
  selector: 'app-student-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MajorLabelPipe,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="student-form">
      <div class="page-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button> 
        <h1>{{ isEditMode ? 'Modifier l\\'étudiant' : 'Ajouter un étudiant' }}</h1>
      </div>

      @if (isLoadingStudent()) {
        <app-loading-spinner message="Chargement..."></app-loading-spinner>
      } @else {
        <mat-card>
          <mat-card-content>
            <form [formGroup]="studentForm" (ngSubmit)="onSubmit()">
              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Prénom</mat-label>
                  <input matInput formControlName="firstName" />
                  @if (studentForm.get('firstName')?.hasError('required')) {
                    <mat-error>Le prénom est requis</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Nom</mat-label>
                  <input matInput formControlName="lastName" />
                  @if (studentForm.get('lastName')?.hasError('required')) {
                    <mat-error>Le nom est requis</mat-error>
                  }
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Email</mat-label>
                  <input matInput formControlName="email" type="email" />
                  @if (studentForm.get('email')?.hasError('required')) {
                    <mat-error>L'email est requis</mat-error>
                  }
                  @if (studentForm.get('email')?.hasError('email')) {
                    <mat-error>Format d'email invalide</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Filière</mat-label>
                  <mat-select formControlName="major">
                    @for (major of majors; track major) {
                      <mat-option [value]="major">{{ major | majorLabel }}</mat-option>
                    }
                  </mat-select>
                  @if (studentForm.get('major')?.hasError('required')) {
                    <mat-error>La filière est requise</mat-error>
                  }
                </mat-form-field>
              </div>

              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Niveau</mat-label>
                  <input matInput formControlName="level" type="number" min="1" max="5" />
                  @if (studentForm.get('level')?.hasError('required')) {
                    <mat-error>Le niveau est requis</mat-error>
                  }
                  @if (studentForm.get('level')?.hasError('min')) {
                    <mat-error>Le niveau minimum est 1</mat-error>
                  }
                  @if (studentForm.get('level')?.hasError('max')) {
                    <mat-error>Le niveau maximum est 5</mat-error>
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
                  [disabled]="studentForm.invalid || isSubmitting()"
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
    .student-form {
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
export class StudentFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly studentService = inject(StudentService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  studentForm: FormGroup = this.fb.group({
    userId: [null],
    firstName: ['', Validators.required],
    lastName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    major: ['', Validators.required],
    level: [1, [Validators.required, Validators.min(1), Validators.max(5)]]
  });

  majors = Object.values(Major);
  isEditMode = false;
  studentId: number | null = null;
  isLoadingStudent = signal(false);
  isSubmitting = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      this.isEditMode = true;
      this.studentId = +id;
      this.loadStudent();
    }
  }

  private loadStudent(): void {
    if (!this.studentId) return;

    this.isLoadingStudent.set(true);

    this.studentService.getById(this.studentId).subscribe({
      next: (student) => {
        this.studentForm.patchValue({
          userId: student.userId ?? null,
          firstName: student.firstName,
          lastName: student.lastName,
          email: student.email,
          major: student.major,
          level: student.level
        });
        this.isLoadingStudent.set(false);
      },
      error: () => {
        this.isLoadingStudent.set(false);
        this.snackBar.open('Erreur lors du chargement de l\'étudiant', 'Fermer', {
          duration: 5000
        });
        this.goBack();
      }
    });
  }

  onSubmit(): void {
    if (this.studentForm.invalid) {
      this.studentForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);

    const formData = this.studentForm.getRawValue();

    const request$ = this.isEditMode
      ? this.studentService.update(this.studentId!, formData)
      : this.studentService.create(formData);

    request$.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.snackBar.open(
          this.isEditMode ? 'Étudiant mis à jour' : 'Étudiant créé',
          'Fermer',
          { duration: 3000 }
        );
        this.goBack();
      },
      error: (error) => {
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
    this.router.navigate(['/dashboard/students']);
  }
}