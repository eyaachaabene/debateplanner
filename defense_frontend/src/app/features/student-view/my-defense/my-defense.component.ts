import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DefenseService, StudentService } from '@core/services';
import { Defense } from '@core/models';
import { StatusBadgeComponent, LoadingSpinnerComponent, EmptyStateComponent } from '@shared/components';

@Component({
  selector: 'app-my-defense',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatDividerModule,
    MatSnackBarModule,
    StatusBadgeComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  template: `
    <div class="my-defense">
      <h1>Ma soutenance</h1>

      @if (isLoading()) {
        <app-loading-spinner message="Chargement de votre soutenance..."></app-loading-spinner>
      } @else if (!defense()) {
        <app-empty-state
          icon="event_busy"
          title="Aucune soutenance planifiée"
          message="Votre soutenance n'a pas encore été planifiée."
        ></app-empty-state>
      } @else {
        @if (defense(); as currentDefense) {
          <div class="defense-details">
            <mat-card class="main-card">
              <div class="status-header">
                <app-status-badge type="status" [status]="currentDefense.status"></app-status-badge>
              </div>

              <mat-card-content>
                <div class="date-time-section">
                  <div class="info-block">
                    <mat-icon>event</mat-icon>
                    <div>
                      <span class="label">Date</span>
                      <span class="value">
                        {{ currentDefense.defenseDate | date:'EEEE dd MMMM yyyy':'':'fr' }}
                      </span>
                    </div>
                  </div>

                  <div class="info-block">
                    <mat-icon>schedule</mat-icon>
                    <div>
                      <span class="label">Horaire</span>
                      <span class="value">
                        {{ currentDefense.startTime }} - {{ currentDefense.endTime }}
                      </span>
                    </div>
                  </div>

                  <div class="info-block">
                    <mat-icon>meeting_room</mat-icon>
                    <div>
                      <span class="label">Salle</span>
                      <span class="value">Salle #{{ currentDefense.roomId }}</span>
                    </div>
                  </div>
                </div>

                <mat-divider></mat-divider>

                <div class="project-section">
                  <h3>Projet</h3>
                  <p>{{ currentDefense.projectTitle }}</p>
                </div>
              </mat-card-content>
            </mat-card>

            <mat-card class="jury-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon>people</mat-icon>
                  Composition du jury
                </mat-card-title>
              </mat-card-header>

              <mat-card-content>
                <div class="jury-item">
                  <span class="label">Encadrant</span>
                  <span class="value">Professeur #{{ currentDefense.supervisorId }}</span>
                </div>

                <mat-divider></mat-divider>

                <div class="jury-item">
                  <span class="label">Président</span>
                  <span class="value">Professeur #{{ currentDefense.presidentId }}</span>
                </div>

                <mat-divider></mat-divider>

                <div class="jury-item">
                  <span class="label">Rapporteur</span>
                  <span class="value">Professeur #{{ currentDefense.reviewerId }}</span>
                </div>

                <mat-divider></mat-divider>

                <div class="jury-item">
                  <span class="label">Examinateur</span>
                  <span class="value">Professeur #{{ currentDefense.examinerId }}</span>
                </div>
              </mat-card-content>
            </mat-card>

            <mat-card class="info-card">
              <mat-card-header>
                <mat-card-title>
                  <mat-icon>info</mat-icon>
                  Informations importantes
                </mat-card-title>
              </mat-card-header>

              <mat-card-content>
                <ul class="info-list">
                  <li>
                    <mat-icon>check_circle</mat-icon>
                    Présentez-vous 15 minutes avant l'heure prévue.
                  </li>
                  <li>
                    <mat-icon>check_circle</mat-icon>
                    Préparez vos supports de présentation à l'avance.
                  </li>
                  <li>
                    <mat-icon>check_circle</mat-icon>
                    Vérifiez votre horaire et votre salle avant le jour J.
                  </li>
                </ul>
              </mat-card-content>
            </mat-card>
          </div>
        }
      }
    </div>
  `,
  styles: [`
    .my-defense {
      max-width: 900px;
      margin: 0 auto;
    }

    .my-defense h1 {
      font-size: 24px;
      font-weight: 500;
      margin-bottom: 24px;
    }

    .defense-details {
      display: grid;
      gap: 24px;
    }

    .status-header {
      padding: 16px 16px 0;
    }

    .date-time-section {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
      gap: 24px;
      padding: 24px 0;
    }

    .info-block {
      display: flex;
      align-items: flex-start;
      gap: 12px;
    }

    .info-block mat-icon {
      color: #3f51b5;
      margin-top: 4px;
    }

    .label {
      display: block;
      font-size: 12px;
      text-transform: uppercase;
      color: #777;
      margin-bottom: 4px;
    }

    .value {
      font-size: 16px;
      font-weight: 500;
      color: #222;
    }

    .project-section {
      padding: 24px 0;
    }

    .project-section h3 {
      font-size: 12px;
      text-transform: uppercase;
      color: #777;
      margin-bottom: 8px;
    }

    .project-section p {
      margin: 0;
      font-size: 16px;
      line-height: 1.6;
    }

    .jury-card mat-card-title,
    .info-card mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .jury-item {
      display: flex;
      justify-content: space-between;
      gap: 16px;
      padding: 16px 0;
    }

    .jury-item .value {
      text-align: right;
    }

    .info-list {
      list-style: none;
      padding: 0;
      margin: 0;
    }

    .info-list li {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px 0;
      border-bottom: 1px solid #eee;
    }

    .info-list li:last-child {
      border-bottom: none;
    }

    .info-list mat-icon {
      color: #4caf50;
    }
  `]
})
export class MyDefenseComponent implements OnInit {
  private readonly defenseService = inject(DefenseService);
  private readonly studentService = inject(StudentService);
  private readonly snackBar = inject(MatSnackBar);

  defense = signal<Defense | null>(null);
  isLoading = signal(false);

  ngOnInit(): void {
    this.loadMyDefense();
  }

  private loadMyDefense(): void {
    this.isLoading.set(true);

    this.studentService.getMe().subscribe({
      next: (student) => {
        this.defenseService.getByStudent(student.id).subscribe({
          next: (defense) => {
            this.defense.set(defense);
            this.isLoading.set(false);
          },
          error: () => {
            this.defense.set(null);
            this.isLoading.set(false);
          }
        });
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open(
          'Erreur lors du chargement de votre profil étudiant',
          'Fermer',
          { duration: 5000 }
        );
      }
    });
  }
}