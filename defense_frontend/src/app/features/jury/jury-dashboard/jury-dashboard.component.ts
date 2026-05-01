import { Component, inject, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatTabsModule } from '@angular/material/tabs';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DefenseService, ProfessorService } from '@core/services';
import { Defense, Professor } from '@core/models';
import { StatusBadgeComponent, LoadingSpinnerComponent, EmptyStateComponent } from '@shared/components';
import { ActivatedRoute, Router } from '@angular/router';


@Component({
  selector: 'app-jury-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatTabsModule,
    MatButtonModule,
    MatIconModule,
    MatDividerModule,
    MatSnackBarModule,
    StatusBadgeComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  template: `
    <div class="jury-dashboard">
      <h1>Mes participations aux jurys</h1>

      @if (isLoading()) {
        <app-loading-spinner message="Chargement de vos jurys..."></app-loading-spinner>
      } @else {
        <mat-tab-group>
          <mat-tab>
            <ng-template mat-tab-label>
              <mat-icon>upcoming</mat-icon>
              À venir ({{ upcomingDefenses().length }})
            </ng-template>

            @if (upcomingDefenses().length === 0) {
              <app-empty-state
                icon="event_available"
                title="Aucune soutenance à venir"
                message="Vous n'avez pas de soutenance planifiée prochainement."
              ></app-empty-state>
            } @else {
              <div class="defense-cards">
                @for (defense of upcomingDefenses(); track defense.id) {
                  <mat-card class="defense-card">
                    <mat-card-header>
                      <mat-card-title>{{ defense.projectTitle }}</mat-card-title>
                      <mat-card-subtitle>Étudiant #{{ defense.studentId }}</mat-card-subtitle>
                    </mat-card-header>
                    <mat-card-content>
                      <mat-divider></mat-divider>
                      <div class="defense-info">
                        <div class="info-item">
                          <mat-icon>event</mat-icon>
                          <span>{{ defense.defenseDate | date:'dd/MM/yyyy' }}</span>
                        </div>
                        <div class="info-item">
                          <mat-icon>schedule</mat-icon>
                          <span>{{ defense.startTime }} - {{ defense.endTime }}</span>
                        </div>
                        <div class="info-item">
                          <mat-icon>meeting_room</mat-icon>
                          <span>Salle #{{ defense.roomId }}</span>
                        </div>
                      </div>
                      <div class="my-role">
                        <span class="role-label">Mon rôle :</span>
                        <span class="role-badge">{{ getMyRoleLabel(defense) }}</span>
                      </div>
                    </mat-card-content>
                  </mat-card>
                }
              </div>
            }
          </mat-tab>

          <mat-tab>
            <ng-template mat-tab-label>
              <mat-icon>rate_review</mat-icon>
              À noter ({{ toGradeDefenses().length }})
            </ng-template>

            @if (toGradeDefenses().length === 0) {
              <app-empty-state
                icon="grading"
                title="Aucune soutenance à noter"
                message="Vous n'avez pas de soutenance en attente de notation."
              ></app-empty-state>
            } @else {
              <div class="defense-cards">
                @for (defense of toGradeDefenses(); track defense.id) {
                  <mat-card class="defense-card to-grade">
                    <mat-card-header>
                      <mat-card-title>{{ defense.projectTitle }}</mat-card-title>
                      <mat-card-subtitle>Étudiant #{{ defense.studentId }}</mat-card-subtitle>
                    </mat-card-header>
                    <mat-card-content>
                      <mat-divider></mat-divider>
                      <div class="defense-info">
                        <div class="info-item">
                          <mat-icon>event</mat-icon>
                          <span>{{ defense.defenseDate | date:'dd/MM/yyyy' }}</span>
                        </div>
                        <div class="info-item">
                          <mat-icon>meeting_room</mat-icon>
                          <span>Salle #{{ defense.roomId }}</span>
                        </div>
                      </div>
                    </mat-card-content>
                    <mat-card-actions>
                      <button mat-flat-button color="primary" [routerLink]="['/dashboard/jury', defense.id, 'grade']">
                        <mat-icon>grading</mat-icon>
                        Noter cette soutenance
                      </button>
                    </mat-card-actions>
                  </mat-card>
                }
              </div>
            }
          </mat-tab>

          <mat-tab>
            <ng-template mat-tab-label>
              <mat-icon>done_all</mat-icon>
              Terminées ({{ completedDefenses().length }})
            </ng-template>

            @if (completedDefenses().length === 0) {
              <app-empty-state
                icon="history"
                title="Aucune soutenance terminée"
                message="Vous n'avez pas encore participé à une soutenance publiée."
              ></app-empty-state>
            } @else {
              <div class="defense-cards">
                @for (defense of completedDefenses(); track defense.id) {
                  <mat-card class="defense-card completed">
                    <mat-card-header>
                      <mat-card-title>{{ defense.projectTitle }}</mat-card-title>
                      <mat-card-subtitle>Étudiant #{{ defense.studentId }}</mat-card-subtitle>
                    </mat-card-header>
                    <mat-card-content>
                      <mat-divider></mat-divider>
                      <div class="defense-info">
                        <div class="info-item">
                          <mat-icon>event</mat-icon>
                          <span>{{ defense.defenseDate | date:'dd/MM/yyyy' }}</span>
                        </div>
                      </div>
                      @if (defense.finalAverage) {
                        <div class="result-summary">
                          <span class="final-grade">{{ defense.finalAverage }}/20</span>
                          <app-status-badge type="mention" [mention]="defense.mention!"></app-status-badge>
                        </div>
                      }
                    </mat-card-content>
                  </mat-card>
                }
              </div>
            }
          </mat-tab>
        </mat-tab-group>
      }
    </div>
  `,
  styles: [`
    .jury-dashboard h1 {
      font-size: 24px;
      font-weight: 500;
      margin-bottom: 24px;
    }

    .defense-cards {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
      gap: 24px;
      margin-top: 24px;
    }

    .defense-card.to-grade {
      border-left: 4px solid #ff9800;
    }

    .defense-card.completed {
      border-left: 4px solid #4caf50;
    }

    .defense-info {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      margin-top: 16px;
    }

    .info-item {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #666;
      font-size: 14px;
    }

    .info-item mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
      color: #999;
    }

    .my-role {
      margin-top: 16px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 8px;
    }

    .role-label {
      color: #666;
      font-size: 13px;
    }

    .role-badge {
      background: #3f51b5;
      color: white;
      padding: 4px 12px;
      border-radius: 16px;
      font-size: 12px;
      margin-left: 8px;
    }

    .result-summary {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-top: 16px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 8px;
    }

    .final-grade {
      font-size: 24px;
      font-weight: 600;
      color: #3f51b5;
    }

    mat-card-actions {
      padding: 16px;
    }

    mat-card-actions button {
      width: 100%;
    }
  `]
})
export class JuryDashboardComponent implements OnInit {
  private readonly defenseService = inject(DefenseService);
  private readonly professorService = inject(ProfessorService);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);



  allDefenses = signal<Defense[]>([]);
  currentProfessor = signal<Professor | null>(null);
  isLoading = signal(false);

  upcomingDefenses = computed(() =>
    this.allDefenses().filter(d => d.status === 'PLANNED')
  );

  toGradeDefenses = computed(() => {
    const professor = this.currentProfessor();
    if (!professor) return [];

    return this.allDefenses().filter(d =>
      d.status !== 'PUBLISHED' &&
      [d.supervisorId, d.presidentId, d.reviewerId, d.examinerId].includes(professor.id)
    );
  });

  completedDefenses = computed(() =>
    this.allDefenses().filter(d => d.status === 'PUBLISHED')
  );

  ngOnInit(): void {
    // Check if we're coming from grade form
    const fromGrade = this.route.snapshot.queryParamMap.get('fromGrade');
    
    if (fromGrade === 'true') {
      console.log('Coming from grade form, skipping reload');
      // Clear the query param
      this.router.navigate(['/dashboard/jury'], { 
        queryParams: { fromGrade: null },
        replaceUrl: true 
      });
      return;
    }
    
    this.loadMyJuryDefenses();
  }

  private loadMyJuryDefenses(): void {
    this.isLoading.set(true);

    this.professorService.getMe().subscribe({
      next: (professor) => {
        this.currentProfessor.set(professor);
        console.log('Current professor:', professor);

        this.defenseService.getJuryDefenses().subscribe({
          next: (defenses) => {
            console.log('All jury defenses:', defenses);
            const mine = defenses.filter(d =>
              [d.supervisorId, d.presidentId, d.reviewerId, d.examinerId].includes(professor.id)
            );
            console.log('My jury defenses:', mine);
            this.allDefenses.set(mine);
            this.isLoading.set(false);
          },
          error: () => {
            this.isLoading.set(false);
            this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 5000 });
          }
        });
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement du professeur', 'Fermer', { duration: 5000 });
      }
    });
  }

  getMyRoleLabel(defense: Defense): string {
    const professor = this.currentProfessor();
    if (!professor) return '-';

    if (defense.supervisorId === professor.id) return 'Encadrant';
    if (defense.presidentId === professor.id) return 'Président';
    if (defense.reviewerId === professor.id) return 'Rapporteur';
    if (defense.examinerId === professor.id) return 'Examinateur';

    return '-';
  }
}
