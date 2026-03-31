import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatTableModule } from '@angular/material/table';
import { DefenseService, StudentService, ProfessorService, RoomService } from '@core/services';
import { Defense } from '@core/models';
import { StatusBadgeComponent } from '@shared/components';
import { forkJoin } from 'rxjs';

interface DashboardStats {
  totalStudents: number;
  totalProfessors: number;
  totalRooms: number;
  upcomingDefenses: number;
  completedDefenses: number;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatTableModule,
    StatusBadgeComponent
  ],
  template: `
    <div class="dashboard-home">
      <h1>Tableau de bord</h1>

      <div class="stats-grid">
        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon students">
              <mat-icon>people</mat-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats().totalStudents }}</span>
              <span class="stat-label">Étudiants</span>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon professors">
              <mat-icon>person</mat-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats().totalProfessors }}</span>
              <span class="stat-label">Professeurs</span>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon rooms">
              <mat-icon>meeting_room</mat-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats().totalRooms }}</span>
              <span class="stat-label">Salles</span>
            </div>
          </mat-card-content>
        </mat-card>

        <mat-card class="stat-card">
          <mat-card-content>
            <div class="stat-icon upcoming">
              <mat-icon>event</mat-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ stats().upcomingDefenses }}</span>
              <span class="stat-label">Soutenances planifiées</span>
            </div>
          </mat-card-content>
        </mat-card>
      </div>

      <mat-card class="upcoming-card">
        <mat-card-header>
          <mat-card-title>Prochaines soutenances</mat-card-title>
          <button mat-button color="primary" routerLink="/dashboard/defenses">
            Voir tout
          </button>
        </mat-card-header>
        <mat-card-content>
          @if (upcomingDefenses().length > 0) {
            <table mat-table [dataSource]="upcomingDefenses()">
              <ng-container matColumnDef="projectTitle">
                <th mat-header-cell *matHeaderCellDef>Projet</th>
                <td mat-cell *matCellDef="let defense">{{ defense.projectTitle }}</td>
              </ng-container>

              <ng-container matColumnDef="studentId">
                <th mat-header-cell *matHeaderCellDef>Étudiant</th>
                <td mat-cell *matCellDef="let defense">#{{ defense.studentId }}</td>
              </ng-container>

              <ng-container matColumnDef="date">
                <th mat-header-cell *matHeaderCellDef>Date</th>
                <td mat-cell *matCellDef="let defense">
                  {{ defense.defenseDate | date:'dd/MM/yyyy' }}
                </td>
              </ng-container>

              <ng-container matColumnDef="time">
                <th mat-header-cell *matHeaderCellDef>Heure</th>
                <td mat-cell *matCellDef="let defense">
                  {{ defense.startTime }} - {{ defense.endTime }}
                </td>
              </ng-container>

              <ng-container matColumnDef="roomId">
                <th mat-header-cell *matHeaderCellDef>Salle</th>
                <td mat-cell *matCellDef="let defense">
                  #{{ defense.roomId }}
                </td>
              </ng-container>

              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Statut</th>
                <td mat-cell *matCellDef="let defense">
                  <app-status-badge type="status" [status]="defense.status"></app-status-badge>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
            </table>
          } @else {
            <div class="empty-message">
              <mat-icon>event_busy</mat-icon>
              <p>Aucune soutenance à venir</p>
            </div>
          }
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .dashboard-home h1 {
      margin-bottom: 24px;
      font-size: 24px;
      font-weight: 500;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      gap: 24px;
      margin-bottom: 24px;
    }

    .stat-card mat-card-content {
      display: flex;
      align-items: center;
      gap: 16px;
      padding: 16px;
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .stat-icon mat-icon {
      font-size: 28px;
      width: 28px;
      height: 28px;
      color: white;
    }

    .stat-icon.students { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); }
    .stat-icon.professors { background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%); }
    .stat-icon.rooms { background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%); }
    .stat-icon.upcoming { background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%); }

    .stat-info {
      display: flex;
      flex-direction: column;
    }

    .stat-value {
      font-size: 28px;
      font-weight: 600;
      color: #333;
    }

    .stat-label {
      font-size: 14px;
      color: #666;
    }

    .upcoming-card table {
      width: 100%;
    }

    .empty-message {
      text-align: center;
      padding: 32px;
      color: #666;
    }

    .empty-message mat-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      color: #ccc;
    }

    .empty-message p {
      margin-top: 16px;
    }
  `]
})
export class HomeComponent implements OnInit {
  private readonly defenseService = inject(DefenseService);
  private readonly studentService = inject(StudentService);
  private readonly professorService = inject(ProfessorService);
  private readonly roomService = inject(RoomService);

  stats = signal<DashboardStats>({
    totalStudents: 0,
    totalProfessors: 0,
    totalRooms: 0,
    upcomingDefenses: 0,
    completedDefenses: 0
  });

  upcomingDefenses = signal<Defense[]>([]);
  displayedColumns = ['projectTitle', 'studentId', 'date', 'time', 'roomId', 'status'];

  ngOnInit(): void {
    this.loadStats();
    this.loadUpcomingDefenses();
  }

  private loadStats(): void {
    forkJoin({
      students: this.studentService.getAll(),
      professors: this.professorService.getAll(),
      rooms: this.roomService.getAll(),
      defenses: this.defenseService.getAll()
    }).subscribe({
      next: (data) => {
        const defenses = data.defenses;
        const planned = defenses.filter(d => d.status === 'PLANNED').length;
        const published = defenses.filter(d => d.status === 'PUBLISHED').length;

        this.stats.set({
          totalStudents: data.students.length,
          totalProfessors: data.professors.length,
          totalRooms: data.rooms.length,
          upcomingDefenses: planned,
          completedDefenses: published
        });
      }
    });
  }

  private loadUpcomingDefenses(): void {
    this.defenseService.getAll({ status: 'PLANNED' as any }).subscribe({
      next: (defenses) => {
        this.upcomingDefenses.set(defenses.slice(0, 5));
      }
    });
  }
}
