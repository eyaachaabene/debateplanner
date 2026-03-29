import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DefenseService } from '@core/services';
import { Defense } from '@core/models';
import { StatusBadgeComponent, LoadingSpinnerComponent } from '@shared/components';

@Component({
  selector: 'app-defense-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatSnackBarModule,
    StatusBadgeComponent,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="defense-detail">
      <div class="page-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h1>Détails de la soutenance</h1>
      </div>

@if (isLoading()) {
  <app-loading-spinner message="Chargement..."></app-loading-spinner>
} @else {
  @if (defense(); as currentDefense) {
    <div class="detail-grid">
      <mat-card>
        <mat-card-header>
          <mat-card-title>Informations générales</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="info-row">
            <span class="label">Projet</span>
            <span class="value">{{ currentDefense.projectTitle }}</span>
          </div>
          <mat-divider></mat-divider>

          <div class="info-row">
            <span class="label">Statut</span>
            <app-status-badge type="status" [status]="currentDefense.status"></app-status-badge>
          </div>
          <mat-divider></mat-divider>

          <div class="info-row">
            <span class="label">Date</span>
            <span class="value">{{ currentDefense.defenseDate | date:'EEEE dd MMMM yyyy':'':'fr' }}</span>
          </div>
          <mat-divider></mat-divider>

          <div class="info-row">
            <span class="label">Horaire</span>
            <span class="value">{{ currentDefense.startTime }} - {{ currentDefense.endTime }}</span>
          </div>
          <mat-divider></mat-divider>

          <div class="info-row">
            <span class="label">Salle</span>
            <span class="value">Salle #{{ currentDefense.roomId }}</span>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-card>
        <mat-card-header>
          <mat-card-title>Références</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="info-row">
            <span class="label">Étudiant</span>
            <span class="value">Étudiant #{{ currentDefense.studentId }}</span>
          </div>
          <mat-divider></mat-divider>

          <div class="info-row">
            <span class="label">Encadrant</span>
            <span class="value">Professeur #{{ currentDefense.supervisorId }}</span>
          </div>
          <mat-divider></mat-divider>

          <div class="info-row">
            <span class="label">Président</span>
            <span class="value">Professeur #{{ currentDefense.presidentId }}</span>
          </div>
          <mat-divider></mat-divider>

          <div class="info-row">
            <span class="label">Rapporteur</span>
            <span class="value">Professeur #{{ currentDefense.reviewerId }}</span>
          </div>
          <mat-divider></mat-divider>

          <div class="info-row">
            <span class="label">Examinateur</span>
            <span class="value">Professeur #{{ currentDefense.examinerId }}</span>
          </div>
        </mat-card-content>
      </mat-card>

      @if (currentDefense.status === 'COMPLETED') {
        <mat-card class="results-card">
          <mat-card-header>
            <mat-card-title>Résultats</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="info-row">
              <span class="label">Moyenne finale</span>
              <span class="value">{{ currentDefense.finalAverage ?? '-' }}/20</span>
            </div>
            <mat-divider></mat-divider>

            <div class="info-row">
              <span class="label">Mention</span>
              <app-status-badge
                type="mention"
                [mention]="currentDefense.mention!"
              ></app-status-badge>
            </div>
            <mat-divider></mat-divider>

            <div class="info-row">
              <span class="label">Note encadrant</span>
              <span class="value">{{ currentDefense.supervisorGrade ?? '-' }}/20</span>
            </div>
            <mat-divider></mat-divider>

            <div class="info-row">
              <span class="label">Note président</span>
              <span class="value">{{ currentDefense.presidentGrade ?? '-' }}/20</span>
            </div>
            <mat-divider></mat-divider>

            <div class="info-row">
              <span class="label">Note rapporteur</span>
              <span class="value">{{ currentDefense.reviewerGrade ?? '-' }}/20</span>
            </div>
            <mat-divider></mat-divider>

            <div class="info-row">
              <span class="label">Note examinateur</span>
              <span class="value">{{ currentDefense.examinerGrade ?? '-' }}/20</span>
            </div>
          </mat-card-content>
        </mat-card>
      }
    </div>
  }
}
    </div>
  `,
  styles: [`
    .defense-detail {
      max-width: 1000px;
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

    .detail-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 24px;
    }

    .results-card {
      grid-column: span 2;
    }

    .info-row {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      gap: 16px;
      padding: 12px 0;
    }

    .label {
      color: #666;
      font-size: 14px;
    }

    .value {
      font-weight: 500;
      color: #333;
      text-align: right;
    }

    @media (max-width: 768px) {
      .results-card {
        grid-column: span 1;
      }
    }
  `]
})
export class DefenseDetailComponent implements OnInit {
  private readonly defenseService = inject(DefenseService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  defense = signal<Defense | null>(null);
  isLoading = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadDefense(+id);
    }
  }

  private loadDefense(id: number): void {
    this.isLoading.set(true);

    this.defenseService.getById(id).subscribe({
      next: (defense) => {
        this.defense.set(defense);
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 5000 });
        this.goBack();
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard/defenses']);
  }
}