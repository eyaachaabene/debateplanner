import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DefenseService } from '@core/services';
import { Defense } from '@core/models';
import { MentionLabelPipe } from '@shared/pipes';
import { StatusBadgeComponent, LoadingSpinnerComponent, EmptyStateComponent } from '@shared/components';

@Component({
  selector: 'app-my-results',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatDividerModule,
    MatSnackBarModule,
    MentionLabelPipe,
    StatusBadgeComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  template: `
    <div class="my-results">
      <h1>Mes résultats</h1>

      @if (isLoading()) {
        <app-loading-spinner message="Chargement des résultats..."></app-loading-spinner>
      } @else if (!defense()) {
        <app-empty-state
          icon="hourglass_empty"
          title="Résultats non disponibles"
          message="Les résultats de votre soutenance ne sont pas encore disponibles."
        ></app-empty-state>
      } @else {
        @if (defense(); as currentDefense) {
          <div class="results-container">
            <mat-card class="main-result">
              <mat-card-content>
                <div class="result-header">
                  <div class="grade-circle" [class]="getGradeClass(currentDefense.finalAverage)">
                    <span class="grade-value">{{ currentDefense.finalAverage ?? '-' }}</span>
                    <span class="grade-max">/20</span>
                  </div>

                  <div class="mention-display">
                    <span class="label">Mention obtenue</span>
                    @if (currentDefense.mention) {
                      <app-status-badge type="mention" [mention]="currentDefense.mention"></app-status-badge>
                    } @else {
                      <span class="fallback-value">-</span>
                    }
                  </div>
                </div>

                <mat-divider></mat-divider>

                <div class="result-message">
                  @if ((currentDefense.finalAverage ?? 0) >= 10) {
                    <mat-icon class="success">celebration</mat-icon>
                    <h2>Félicitations !</h2>
                    <p>Votre soutenance a été validée.</p>
                  } @else {
                    <mat-icon class="warning">sentiment_dissatisfied</mat-icon>
                    <h2>Soutenance ajournée</h2>
                    <p>Veuillez vous rapprocher de votre encadrant pour la suite.</p>
                  }
                </div>
              </mat-card-content>
            </mat-card>

            <mat-card class="defense-summary">
              <mat-card-header>
                <mat-card-title>Récapitulatif de la soutenance</mat-card-title>
              </mat-card-header>

              <mat-card-content>
                <div class="summary-item">
                  <span class="label">Projet</span>
                  <span class="value">{{ currentDefense.projectTitle }}</span>
                </div>

                <mat-divider></mat-divider>

                <div class="summary-item">
                  <span class="label">Date de soutenance</span>
                  <span class="value">
                    {{ currentDefense.defenseDate | date:'dd MMMM yyyy':'':'fr' }}
                  </span>
                </div>

                <mat-divider></mat-divider>

                <div class="summary-item">
                  <span class="label">Horaire</span>
                  <span class="value">
                    {{ currentDefense.startTime }} - {{ currentDefense.endTime }}
                  </span>
                </div>

                <mat-divider></mat-divider>

                <div class="summary-item">
                  <span class="label">Salle</span>
                  <span class="value">Salle #{{ currentDefense.roomId }}</span>
                </div>

                <mat-divider></mat-divider>

                <div class="summary-item">
                  <span class="label">Statut</span>
                  <div class="value">
                    <app-status-badge type="status" [status]="currentDefense.status"></app-status-badge>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>

            <mat-card class="jury-evaluation">
              <mat-card-header>
                <mat-card-title>Notes du jury</mat-card-title>
              </mat-card-header>

              <mat-card-content>
                <div class="jury-grade-item">
                  <span class="label">Encadrant</span>
                  <span class="value">{{ currentDefense.supervisorGrade ?? '-' }}/20</span>
                </div>

                <mat-divider></mat-divider>

                <div class="jury-grade-item">
                  <span class="label">Président</span>
                  <span class="value">{{ currentDefense.presidentGrade ?? '-' }}/20</span>
                </div>

                <mat-divider></mat-divider>

                <div class="jury-grade-item">
                  <span class="label">Rapporteur</span>
                  <span class="value">{{ currentDefense.reviewerGrade ?? '-' }}/20</span>
                </div>

                <mat-divider></mat-divider>

                <div class="jury-grade-item">
                  <span class="label">Examinateur</span>
                  <span class="value">{{ currentDefense.examinerGrade ?? '-' }}/20</span>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .my-results {
      max-width: 800px;
      margin: 0 auto;
    }

    .my-results h1 {
      font-size: 24px;
      font-weight: 500;
      margin-bottom: 24px;
    }

    .results-container {
      display: grid;
      gap: 24px;
    }

    .main-result {
      text-align: center;
    }

    .result-header {
      display: flex;
      justify-content: center;
      align-items: center;
      gap: 48px;
      padding: 32px 0;
      flex-wrap: wrap;
    }

    .grade-circle {
      width: 140px;
      height: 140px;
      border-radius: 50%;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
    }

    .grade-circle.excellent {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }

    .grade-circle.good {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }

    .grade-circle.pass {
      background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
    }

    .grade-circle.fail {
      background: linear-gradient(135deg, #ff0844 0%, #ffb199 100%);
    }

    .grade-value {
      font-size: 48px;
      font-weight: 700;
      color: white;
    }

    .grade-max {
      font-size: 20px;
      color: rgba(255, 255, 255, 0.9);
    }

    .mention-display .label {
      display: block;
      font-size: 12px;
      text-transform: uppercase;
      color: #999;
      margin-bottom: 8px;
    }

    .fallback-value {
      font-weight: 500;
      color: #333;
    }

    .result-message {
      padding: 32px 0;
    }

    .result-message mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
    }

    .result-message mat-icon.success {
      color: #4caf50;
    }

    .result-message mat-icon.warning {
      color: #ff9800;
    }

    .result-message h2 {
      font-size: 24px;
      font-weight: 500;
      margin: 0 0 8px 0;
    }

    .result-message p {
      color: #666;
      margin: 0;
    }

    .summary-item,
    .jury-grade-item {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 16px;
      padding: 16px 0;
    }

    .summary-item .label,
    .jury-grade-item .label {
      color: #666;
      font-size: 14px;
    }

    .summary-item .value,
    .jury-grade-item .value {
      font-weight: 500;
      color: #333;
      text-align: right;
    }
  `]
})
export class MyResultsComponent implements OnInit {
  private readonly defenseService = inject(DefenseService);
  private readonly snackBar = inject(MatSnackBar);

  defense = signal<Defense | null>(null);
  isLoading = signal(false);

  ngOnInit(): void {
    this.loadMyResult();
  }

  private loadMyResult(): void {
    this.isLoading.set(true);

    this.defenseService.getMyResult().subscribe({
      next: (defense) => {
        this.defense.set(defense);
        this.isLoading.set(false);
      },
      error: () => {
        this.defense.set(null);
        this.isLoading.set(false);
      }
    });
  }

  getGradeClass(grade?: number): string {
    if (grade == null) return '';
    if (grade >= 16) return 'excellent';
    if (grade >= 14) return 'good';
    if (grade >= 10) return 'pass';
    return 'fail';
  }
}