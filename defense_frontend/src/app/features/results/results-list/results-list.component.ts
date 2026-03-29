import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { DefenseService } from '@core/services';
import { Defense } from '@core/models';
import { StatusBadgeComponent, LoadingSpinnerComponent, EmptyStateComponent } from '@shared/components';

@Component({
  selector: 'app-results-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    MatSnackBarModule,
    StatusBadgeComponent,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  template: `
    <div class="results-list">
      <div class="page-header">
        <h1>Résultats des soutenances</h1>
      </div>

      <mat-card>
        <mat-card-content>
          <div class="filters">
            <mat-form-field appearance="outline">
              <mat-label>Date début</mat-label>
              <input matInput [matDatepicker]="startPicker" [(ngModel)]="startDate" (dateChange)="applyFilter()">
              <mat-datepicker-toggle matIconSuffix [for]="startPicker"></mat-datepicker-toggle>
              <mat-datepicker #startPicker></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Date fin</mat-label>
              <input matInput [matDatepicker]="endPicker" [(ngModel)]="endDate" (dateChange)="applyFilter()">
              <mat-datepicker-toggle matIconSuffix [for]="endPicker"></mat-datepicker-toggle>
              <mat-datepicker #endPicker></mat-datepicker>
            </mat-form-field>

            @if (startDate || endDate) {
              <button mat-button (click)="clearFilters()">
                <mat-icon>clear</mat-icon>
                Effacer
              </button>
            }
          </div>

          @if (isLoading()) {
            <app-loading-spinner message="Chargement des résultats..."></app-loading-spinner>
          } @else if (filteredResults().length === 0) {
            <app-empty-state
              icon="assessment"
              title="Aucun résultat"
              message="Il n'y a pas encore de soutenance publiée."
            ></app-empty-state>
          } @else {
            <div class="stats-summary">
              <div class="stat">
                <span class="stat-value">{{ stats().total }}</span>
                <span class="stat-label">Soutenances</span>
              </div>
              <div class="stat success">
                <span class="stat-value">{{ stats().passed }}</span>
                <span class="stat-label">Réussies</span>
              </div>
              <div class="stat warning">
                <span class="stat-value">{{ stats().failed }}</span>
                <span class="stat-label">Ajournées</span>
              </div>
              <div class="stat">
                <span class="stat-value">{{ stats().average }}/20</span>
                <span class="stat-label">Moyenne</span>
              </div>
            </div>

            <table mat-table [dataSource]="dataSource">
              <ng-container matColumnDef="projectTitle">
                <th mat-header-cell *matHeaderCellDef>Projet</th>
                <td mat-cell *matCellDef="let result">{{ result.projectTitle }}</td>
              </ng-container>

              <ng-container matColumnDef="studentId">
                <th mat-header-cell *matHeaderCellDef>Étudiant</th>
                <td mat-cell *matCellDef="let result">#{{ result.studentId }}</td>
              </ng-container>

              <ng-container matColumnDef="date">
                <th mat-header-cell *matHeaderCellDef>Date</th>
                <td mat-cell *matCellDef="let result">
                  {{ result.defenseDate | date:'dd/MM/yyyy' }}
                </td>
              </ng-container>

              <ng-container matColumnDef="finalAverage">
                <th mat-header-cell *matHeaderCellDef>Note</th>
                <td mat-cell *matCellDef="let result">
                  <span class="grade">
                    {{ result.finalAverage }}/20
                  </span>
                </td>
              </ng-container>

              <ng-container matColumnDef="mention">
                <th mat-header-cell *matHeaderCellDef>Mention</th>
                <td mat-cell *matCellDef="let result">
                  <app-status-badge type="mention" [mention]="result.mention!"></app-status-badge>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
            </table>
          }
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .results-list .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .results-list .page-header h1 {
      font-size: 24px;
      font-weight: 500;
      margin: 0;
    }

    .filters {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      align-items: center;
    }

    .filters mat-form-field {
      width: 180px;
    }

    .stats-summary {
      display: flex;
      gap: 24px;
      margin-bottom: 24px;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 8px;
    }

    .stat {
      text-align: center;
    }

    .stat-value {
      display: block;
      font-size: 28px;
      font-weight: 600;
      color: #333;
    }

    .stat-label {
      font-size: 13px;
      color: #666;
    }

    .stat.success .stat-value {
      color: #2e7d32;
    }

    .stat.warning .stat-value {
      color: #c62828;
    }

    table {
      width: 100%;
    }

    .grade {
      font-weight: 600;
      padding: 4px 8px;
      border-radius: 4px;
      background: #f5f5f5;
      color: #333;
    }
  `]
})
export class ResultsListComponent implements OnInit {
  private readonly defenseService = inject(DefenseService);
  private readonly snackBar = inject(MatSnackBar);

  dataSource = new MatTableDataSource<Defense>([]);
  displayedColumns = ['projectTitle', 'studentId', 'date', 'finalAverage', 'mention'];

  isLoading = signal(false);
  allResults = signal<Defense[]>([]);
  filteredResults = signal<Defense[]>([]);
  startDate: Date | null = null;
  endDate: Date | null = null;

  stats = signal({
    total: 0,
    passed: 0,
    failed: 0,
    average: 0
  });

  ngOnInit(): void {
    this.loadResults();
  }

  loadResults(): void {
    this.isLoading.set(true);

    this.defenseService.getAll({ status: 'PUBLISHED' as any }).subscribe({
      next: (results) => {
        this.allResults.set(results);
        this.applyFilter();
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 5000 });
      }
    });
  }

  applyFilter(): void {
    let filtered = [...this.allResults()];

    if (this.startDate) {
      const start = this.formatDate(this.startDate);
      filtered = filtered.filter(r => r.defenseDate >= start);
    }

    if (this.endDate) {
      const end = this.formatDate(this.endDate);
      filtered = filtered.filter(r => r.defenseDate <= end);
    }

    this.filteredResults.set(filtered);
    this.dataSource.data = filtered;
    this.calculateStats(filtered);
  }

  private calculateStats(results: Defense[]): void {
    const total = results.length;
    const passed = results.filter(r => (r.finalAverage ?? 0) >= 10).length;
    const failed = total - passed;
    const average = total > 0
      ? Math.round((results.reduce((sum, r) => sum + (r.finalAverage ?? 0), 0) / total) * 10) / 10
      : 0;

    this.stats.set({ total, passed, failed, average });
  }

  clearFilters(): void {
    this.startDate = null;
    this.endDate = null;
    this.applyFilter();
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }
}