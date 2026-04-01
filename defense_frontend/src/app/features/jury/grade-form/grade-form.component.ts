import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSliderModule } from '@angular/material/slider';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DefenseService, ProfessorService } from '@core/services';
import { Defense, Professor } from '@core/models';
import { LoadingSpinnerComponent } from '@shared/components';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-grade-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSliderModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="grade-form">
      <div class="page-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h1>Noter la soutenance</h1>
      </div>

    @if (isLoadingDefense()) {
      <app-loading-spinner message="Chargement..."></app-loading-spinner>
    } @else {
      @if (defense(); as currentDefense) {
        <div class="grade-layout">
          <mat-card class="info-card">
            <mat-card-header>
              <mat-card-title>Informations de la soutenance</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <div class="info-section">
                <h3>Projet</h3>
                <p>{{ currentDefense.projectTitle }}</p>
              </div>

              <mat-divider></mat-divider>

              <div class="info-section">
                <h3>Références</h3>
                <p><strong>Étudiant :</strong> #{{ currentDefense.studentId }}</p>
                <p><strong>Salle :</strong> #{{ currentDefense.roomId }}</p>
              </div>

              <mat-divider></mat-divider>

              <div class="info-section">
                <h3>Date et horaire</h3>
                <p>
                  <mat-icon>event</mat-icon>
                  {{ currentDefense.defenseDate | date:'dd MMMM yyyy':'':'fr' }}
                </p>
                <p>
                  <mat-icon>schedule</mat-icon>
                  {{ currentDefense.startTime }} - {{ currentDefense.endTime }}
                </p>
              </div>

              <mat-divider></mat-divider>

              <div class="info-section">
                <h3>Jury</h3>
                <p><strong>Encadrant :</strong> Professeur #{{ currentDefense.supervisorId }}</p>
                <p><strong>Président :</strong> Professeur #{{ currentDefense.presidentId }}</p>
                <p><strong>Rapporteur :</strong> Professeur #{{ currentDefense.reviewerId }}</p>
                <p><strong>Examinateur :</strong> Professeur #{{ currentDefense.examinerId }}</p>
              </div>
            </mat-card-content>
          </mat-card>

          <mat-card class="grade-card">
            <mat-card-header>
              <mat-card-title>Votre évaluation</mat-card-title>
            </mat-card-header>
            <mat-card-content>
              <form [formGroup]="gradeForm" (ngSubmit)="onSubmit()">
                <div class="grade-input">
                  <label>Note (sur 20)</label>
                  <div class="grade-slider">
                    <mat-slider min="0" max="20" step="0.5" discrete>
                      <input matSliderThumb formControlName="grade">
                    </mat-slider>
                    <span class="grade-display">{{ gradeForm.get('grade')?.value }}/20</span>
                  </div>
                </div>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Commentaires</mat-label>
                  <textarea
                    matInput
                    formControlName="comments"
                    rows="6"
                    placeholder="Commentaires internes éventuels"
                  ></textarea>
                  <mat-hint>Minimum 10 caractères</mat-hint>
                  @if (gradeForm.get('comments')?.hasError('required')) {
                    <mat-error>Les commentaires sont requis</mat-error>
                  }
                  @if (gradeForm.get('comments')?.hasError('minlength')) {
                    <mat-error>Minimum 10 caractères requis</mat-error>
                  }
                </mat-form-field>

                <div class="actions">
                  <button mat-button type="button" (click)="goBack()">Annuler</button>
                  <button
                    mat-flat-button
                    color="primary"
                    type="submit"
                    [disabled]="gradeForm.invalid || isSubmitting()"
                  >
                    @if (isSubmitting()) {
                      <mat-spinner diameter="20"></mat-spinner>
                    } @else {
                      <mat-icon>send</mat-icon>
                      Soumettre ma note
                    }
                  </button>
                </div>
              </form>
            </mat-card-content>
          </mat-card>
        </div>
      }
    }
    </div>
  `,
  styles: [`
    .grade-form {
      max-width: 1200px;
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

    .grade-layout {
      display: grid;
      grid-template-columns: 1fr 1.5fr;
      gap: 24px;
    }

    .info-section {
      padding: 16px 0;
    }

    .info-section h3 {
      font-size: 12px;
      text-transform: uppercase;
      color: #999;
      margin-bottom: 8px;
    }

    .info-section p {
      margin: 8px 0;
      color: #333;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .grade-input {
      margin-bottom: 24px;
    }

    .grade-input label {
      display: block;
      margin-bottom: 16px;
      font-weight: 500;
      color: #333;
    }

    .grade-slider {
      display: flex;
      align-items: center;
      gap: 24px;
    }

    .grade-slider mat-slider {
      flex: 1;
    }

    .grade-display {
      font-size: 32px;
      font-weight: 600;
      color: #3f51b5;
      min-width: 80px;
      text-align: center;
    }

    .full-width {
      width: 100%;
    }

    .actions {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
    }

    @media (max-width: 900px) {
      .grade-layout {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class GradeFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly defenseService = inject(DefenseService);
  private readonly professorService = inject(ProfessorService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  gradeForm: FormGroup = this.fb.group({
    grade: [14, [Validators.required, Validators.min(0), Validators.max(20)]],
    comments: ['', [Validators.required, Validators.minLength(10)]]
  });

  defense = signal<Defense | null>(null);
  currentProfessor = signal<Professor | null>(null);
  defenseId: number | null = null;
  isLoadingDefense = signal(false);
  isSubmitting = signal(false);

  ngOnInit(): void {
    this.professorService.getMe().subscribe({
      next: (professor) => this.currentProfessor.set(professor)
    });

    const id = this.route.snapshot.paramMap.get('defenseId');
    if (id) {
      this.defenseId = +id;
      this.loadDefense();
    }
  }

  private loadDefense(): void {
    if (!this.defenseId) return;

    this.isLoadingDefense.set(true);
    this.defenseService.getById(this.defenseId).subscribe({
      next: (defense) => {
        this.defense.set(defense);
        this.isLoadingDefense.set(false);
      },
      error: () => {
        this.isLoadingDefense.set(false);
        this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 5000 });
        this.goBack();
      }
    });
  }

  onSubmit(): void {
    if (this.gradeForm.invalid || !this.defenseId) return;

    const professor = this.currentProfessor();
    const defense = this.defense();

    if (!professor || !defense) {
      this.snackBar.open('Impossible de déterminer le rôle du professeur', 'Fermer', { duration: 5000 });
      return;
    }

    this.isSubmitting.set(true);
    const payload = {
      grade: this.gradeForm.get('grade')?.value
    };

    let request$: Observable<any>;

    if (defense.presidentId === professor.id) {
      request$ = this.defenseService.submitPresidentGrade(this.defenseId, payload.grade);
    } else if (defense.reviewerId === professor.id) {
      request$ = this.defenseService.submitReviewerGrade(this.defenseId, payload.grade);
    } else if (defense.examinerId === professor.id) {
      request$ = this.defenseService.submitExaminerGrade(this.defenseId, payload.grade);
    } else if (defense.supervisorId === professor.id) {
      request$ = this.defenseService.submitSupervisorGrade(this.defenseId, payload.grade);
    } else {
      this.isSubmitting.set(false);
      this.snackBar.open('Vous n’êtes pas membre du jury de cette soutenance', 'Fermer', { duration: 5000 });
      return;
    }

    request$.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.snackBar.open('Note soumise avec succès', 'Fermer', { duration: 3000 });
        this.goBack();
      },
      error: (error: { error: { message: any; }; }) => {
        this.isSubmitting.set(false);
        this.snackBar.open(
          error?.error?.message || 'Erreur lors de la soumission',
          'Fermer',
          { duration: 5000 }
        );
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard/jury']);
  }
}