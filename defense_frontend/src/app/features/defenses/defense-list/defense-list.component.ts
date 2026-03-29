import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { DefenseService } from '@core/services';
import { Defense, DefenseStatus } from '@core/models';
import { DefenseStatusLabelPipe } from '@shared/pipes';
import { ConfirmDialogComponent, LoadingSpinnerComponent, EmptyStateComponent, StatusBadgeComponent } from '@shared/components';

@Component({
  selector: 'app-defense-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatTableModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    DefenseStatusLabelPipe,
    LoadingSpinnerComponent,
    EmptyStateComponent,
    StatusBadgeComponent
  ],
  template: `
    <div class="defense-list">
      <div class="page-header">
        <h1>Gestion des soutenances</h1>
        <button mat-flat-button color="primary" routerLink="/dashboard/defenses/new">
          <mat-icon>add</mat-icon>
          Planifier une soutenance
        </button>
      </div>

      <mat-card>
        <mat-card-content>
          <div class="filters">
            <mat-form-field appearance="outline">
              <mat-label>Statut</mat-label>
              <mat-select [(ngModel)]="selectedStatus" (selectionChange)="loadDefenses()">
                <mat-option [value]="null">Tous les statuts</mat-option>
                @for (status of statuses; track status) {
                  <mat-option [value]="status">{{ status | defenseStatusLabel }}</mat-option>
                }
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Date</mat-label>
              <input matInput [matDatepicker]="picker" [(ngModel)]="selectedDate" (dateChange)="loadDefenses()">
              <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
              <mat-datepicker #picker></mat-datepicker>
            </mat-form-field>

            @if (selectedDate || selectedStatus) {
              <button mat-button (click)="clearFilters()">
                <mat-icon>clear</mat-icon>
                Effacer les filtres
              </button>
            }
          </div>

          @if (isLoading()) {
            <app-loading-spinner message="Chargement des soutenances..."></app-loading-spinner>
          } @else if (dataSource.data.length === 0) {
            <app-empty-state
              icon="event"
              title="Aucune soutenance"
              message="Il n'y a pas encore de soutenance planifiée."
              actionLabel="Planifier une soutenance"
              [action]="goToAddDefense"
            ></app-empty-state>
          } @else {
            <table mat-table [dataSource]="dataSource">
              <ng-container matColumnDef="projectTitle">
                <th mat-header-cell *matHeaderCellDef>Projet</th>
                <td mat-cell *matCellDef="let defense">{{ defense.projectTitle }}</td>
              </ng-container>

              <ng-container matColumnDef="studentId">
                <th mat-header-cell *matHeaderCellDef>Étudiant</th>
                <td mat-cell *matCellDef="let defense">Étudiant #{{ defense.studentId }}</td>
              </ng-container>

              <ng-container matColumnDef="date">
                <th mat-header-cell *matHeaderCellDef>Date</th>
                <td mat-cell *matCellDef="let defense">
                  {{ defense.defenseDate | date:'dd/MM/yyyy' }}
                </td>
              </ng-container>

              <ng-container matColumnDef="time">
                <th mat-header-cell *matHeaderCellDef>Horaire</th>
                <td mat-cell *matCellDef="let defense">
                  {{ defense.startTime }} - {{ defense.endTime }}
                </td>
              </ng-container>

              <ng-container matColumnDef="roomId">
                <th mat-header-cell *matHeaderCellDef>Salle</th>
                <td mat-cell *matCellDef="let defense">Salle #{{ defense.roomId }}</td>
              </ng-container>

              <ng-container matColumnDef="status">
                <th mat-header-cell *matHeaderCellDef>Statut</th>
                <td mat-cell *matCellDef="let defense">
                  <app-status-badge type="status" [status]="defense.status"></app-status-badge>
                </td>
              </ng-container>

              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Actions</th>
                <td mat-cell *matCellDef="let defense">
                  <button
                    mat-icon-button
                    color="primary"
                    [routerLink]="['/dashboard/defenses', defense.id]"
                    matTooltip="Détails"
                  >
                    <mat-icon>visibility</mat-icon>
                  </button>

                  <button
                    mat-icon-button
                    color="primary"
                    [routerLink]="['/dashboard/defenses', defense.id, 'edit']"
                    matTooltip="Modifier"
                    [disabled]="defense.status === 'PUBLISHED'"
                  >
                    <mat-icon>edit</mat-icon>
                  </button>

                  <button
                    mat-icon-button
                    color="warn"
                    (click)="confirmDelete(defense)"
                    matTooltip="Supprimer"
                    [disabled]="defense.status === 'PUBLISHED'"
                  >
                    <mat-icon>delete</mat-icon>
                  </button>
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
    .defense-list .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .defense-list .page-header h1 {
      font-size: 24px;
      font-weight: 500;
      margin: 0;
    }

    .filters {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
      align-items: center;
    }

    .filters mat-form-field {
      width: 220px;
    }

    table {
      width: 100%;
    }
  `]
})
export class DefenseListComponent implements OnInit {
  private readonly defenseService = inject(DefenseService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  dataSource = new MatTableDataSource<Defense>([]);
  displayedColumns: string[] = ['projectTitle', 'studentId', 'date', 'time', 'roomId', 'status', 'actions'];
  statuses = Object.values(DefenseStatus);

  isLoading = signal(false);
  selectedStatus: DefenseStatus | null = null;
  selectedDate: Date | null = null;

  ngOnInit(): void {
    this.loadDefenses();
  }

  loadDefenses(): void {
    this.isLoading.set(true);

    this.defenseService.getAll({
      status: this.selectedStatus || undefined,
      date: this.selectedDate ? this.formatDate(this.selectedDate) : undefined
    }).subscribe({
      next: (defenses) => {
        this.dataSource.data = defenses;
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement des soutenances', 'Fermer', { duration: 5000 });
      }
    });
  }

  clearFilters(): void {
    this.selectedStatus = null;
    this.selectedDate = null;
    this.loadDefenses();
  }

  confirmDelete(defense: Defense): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Supprimer la soutenance',
        message: 'Êtes-vous sûr de vouloir supprimer cette soutenance ?'
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteDefense(defense.id);
      }
    });
  }

  private deleteDefense(id: number): void {
    this.defenseService.delete(id).subscribe({
      next: () => {
        this.snackBar.open('Soutenance supprimée avec succès', 'Fermer', { duration: 3000 });
        this.loadDefenses();
      },
      error: () => {
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', { duration: 5000 });
      }
    });
  }

  private formatDate(date: Date): string {
    return date.toISOString().split('T')[0];
  }

  goToAddDefense = () => {
    this.router.navigate(['/dashboard/defenses/new']);
  };
}