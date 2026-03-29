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
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProfessorService } from '@core/services';
import { Professor } from '@core/models';
import { ConfirmDialogComponent, LoadingSpinnerComponent, EmptyStateComponent } from '@shared/components';

@Component({
  selector: 'app-professor-list',
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
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  template: `
    <div class="professor-list">
      <div class="page-header">
        <h1>Gestion des professeurs</h1>
        <button mat-flat-button color="primary" routerLink="/dashboard/professors/new">
          <mat-icon>add</mat-icon>
          Ajouter un professeur
        </button>
      </div>

      <mat-card>
        <mat-card-content>
          <div class="filters">
            <mat-form-field appearance="outline">
              <mat-label>Rechercher</mat-label>
              <input
                matInput
                [(ngModel)]="searchQuery"
                (input)="applyFilter()"
                placeholder="Nom, prénom, email..."
              />
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>
          </div>

          @if (isLoading()) {
            <app-loading-spinner message="Chargement des professeurs..."></app-loading-spinner>
          } @else if (filteredProfessors().length === 0) {
            <app-empty-state
              icon="person"
              title="Aucun professeur"
              message="Il n'y a pas encore de professeur enregistré."
              actionLabel="Ajouter un professeur"
              [action]="goToAddProfessor"
            ></app-empty-state>
          } @else {
            <table mat-table [dataSource]="dataSource">
              <ng-container matColumnDef="name">
                <th mat-header-cell *matHeaderCellDef>Nom complet</th>
                <td mat-cell *matCellDef="let professor">
                  {{ professor.firstName }} {{ professor.lastName }}
                </td>
              </ng-container>

              <ng-container matColumnDef="email">
                <th mat-header-cell *matHeaderCellDef>Email</th>
                <td mat-cell *matCellDef="let professor">{{ professor.email }}</td>
              </ng-container>

              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Actions</th>
                <td mat-cell *matCellDef="let professor">
                  <button
                    mat-icon-button
                    color="primary"
                    [routerLink]="['/dashboard/professors', professor.id, 'edit']"
                    matTooltip="Modifier"
                  >
                    <mat-icon>edit</mat-icon>
                  </button>

                  <button
                    mat-icon-button
                    color="warn"
                    (click)="confirmDelete(professor)"
                    matTooltip="Supprimer"
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
    .professor-list .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .professor-list .page-header h1 {
      font-size: 24px;
      font-weight: 500;
      margin: 0;
    }

    .filters {
      margin-bottom: 16px;
    }

    .filters mat-form-field {
      width: 100%;
      max-width: 400px;
    }

    table {
      width: 100%;
    }
  `]
})
export class ProfessorListComponent implements OnInit {
  private readonly professorService = inject(ProfessorService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  dataSource = new MatTableDataSource<Professor>([]);
  displayedColumns: string[] = ['name', 'email', 'actions'];

  isLoading = signal(false);
  professors = signal<Professor[]>([]);
  filteredProfessors = signal<Professor[]>([]);
  searchQuery = '';

  ngOnInit(): void {
    this.loadProfessors();
  }

  loadProfessors(): void {
    this.isLoading.set(true);

    this.professorService.getAll().subscribe({
      next: (professors) => {
        this.professors.set(professors);
        this.applyFilter();
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement des professeurs', 'Fermer', {
          duration: 5000
        });
      }
    });
  }

  applyFilter(): void {
    const search = this.searchQuery.trim().toLowerCase();

    const filtered = this.professors().filter(professor => {
      if (!search) return true;

      return (
        professor.firstName.toLowerCase().includes(search) ||
        professor.lastName.toLowerCase().includes(search) ||
        professor.email.toLowerCase().includes(search)
      );
    });

    this.filteredProfessors.set(filtered);
    this.dataSource.data = filtered;
  }

  confirmDelete(professor: Professor): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Supprimer le professeur',
        message: `Êtes-vous sûr de vouloir supprimer ${professor.firstName} ${professor.lastName} ?`
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteProfessor(professor.id);
      }
    });
  }

  private deleteProfessor(id: number): void {
    this.professorService.delete(id).subscribe({
      next: () => {
        this.snackBar.open('Professeur supprimé avec succès', 'Fermer', {
          duration: 3000
        });
        this.loadProfessors();
      },
      error: () => {
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', {
          duration: 5000
        });
      }
    });
  }

  goToAddProfessor = () => {
    this.router.navigate(['/dashboard/professors/new']);
  };
}