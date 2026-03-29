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
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { StudentService } from '@core/services';
import { Student, Major } from '@core/models';
import { MajorLabelPipe } from '@shared/pipes';
import { ConfirmDialogComponent, LoadingSpinnerComponent, EmptyStateComponent } from '@shared/components';

@Component({
  selector: 'app-student-list',
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
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    MajorLabelPipe,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  template: `
    <div class="student-list">
      <div class="page-header">
        <h1>Gestion des étudiants</h1>
        <button mat-flat-button color="primary" routerLink="/dashboard/students/new">
          <mat-icon>add</mat-icon>
          Ajouter un étudiant
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

            <mat-form-field appearance="outline">
              <mat-label>Filière</mat-label>
              <mat-select [(ngModel)]="selectedMajor" (selectionChange)="loadStudents()">
                <mat-option [value]="null">Toutes les filières</mat-option>
                @for (major of majors; track major) {
                  <mat-option [value]="major">{{ major | majorLabel }}</mat-option>
                }
              </mat-select>
            </mat-form-field>
          </div>

          @if (isLoading()) {
            <app-loading-spinner message="Chargement des étudiants..."></app-loading-spinner>
          } @else if (filteredStudents().length === 0) {
            <app-empty-state
              icon="people"
              title="Aucun étudiant"
              message="Il n'y a pas encore d'étudiant enregistré."
              actionLabel="Ajouter un étudiant"
              [action]="goToAddStudent"
            ></app-empty-state>
          } @else {
            <table mat-table [dataSource]="dataSource">
              <ng-container matColumnDef="name">
                <th mat-header-cell *matHeaderCellDef>Nom complet</th>
                <td mat-cell *matCellDef="let student">
                  {{ student.firstName }} {{ student.lastName }}
                </td>
              </ng-container>

              <ng-container matColumnDef="email">
                <th mat-header-cell *matHeaderCellDef>Email</th>
                <td mat-cell *matCellDef="let student">{{ student.email }}</td>
              </ng-container>

              <ng-container matColumnDef="major">
                <th mat-header-cell *matHeaderCellDef>Filière</th>
                <td mat-cell *matCellDef="let student">{{ student.major | majorLabel }}</td>
              </ng-container>

              <ng-container matColumnDef="level">
                <th mat-header-cell *matHeaderCellDef>Niveau</th>
                <td mat-cell *matCellDef="let student">{{ student.level }}</td>
              </ng-container>

              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Actions</th>
                <td mat-cell *matCellDef="let student">
                  <button
                    mat-icon-button
                    color="primary"
                    [routerLink]="['/dashboard/students', student.id, 'edit']"
                    matTooltip="Modifier"
                  >
                    <mat-icon>edit</mat-icon>
                  </button>

                  <button
                    mat-icon-button
                    color="warn"
                    (click)="confirmDelete(student)"
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
    .student-list .page-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .student-list .page-header h1 {
      font-size: 24px;
      font-weight: 500;
      margin: 0;
    }

    .filters {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
    }

    .filters mat-form-field {
      flex: 1;
      max-width: 320px;
    }

    table {
      width: 100%;
    }
  `]
})
export class StudentListComponent implements OnInit {
  private readonly studentService = inject(StudentService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  dataSource = new MatTableDataSource<Student>([]);
  displayedColumns: string[] = ['name', 'email', 'major', 'level', 'actions'];
  majors = Object.values(Major);

  isLoading = signal(false);
  students = signal<Student[]>([]);
  searchQuery = '';
  selectedMajor: Major | null = null;

  filteredStudents = signal<Student[]>([]);

  ngOnInit(): void {
    this.loadStudents();
  }

  loadStudents(): void {
    this.isLoading.set(true);

    this.studentService.getAll({
      major: this.selectedMajor || undefined
    }).subscribe({
      next: (students) => {
        this.students.set(students);
        this.applyFilter();
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement des étudiants', 'Fermer', {
          duration: 5000
        });
      }
    });
  }

  applyFilter(): void {
    const search = this.searchQuery.trim().toLowerCase();

    const filtered = this.students().filter(student => {
      if (!search) return true;

      return (
        student.firstName.toLowerCase().includes(search) ||
        student.lastName.toLowerCase().includes(search) ||
        student.email.toLowerCase().includes(search)
      );
    });

    this.filteredStudents.set(filtered);
    this.dataSource.data = filtered;
  }

  confirmDelete(student: Student): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Supprimer l\'étudiant',
        message: `Êtes-vous sûr de vouloir supprimer ${student.firstName} ${student.lastName} ?`
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteStudent(student.id);
      }
    });
  }

  private deleteStudent(id: number): void {
    this.studentService.delete(id).subscribe({
      next: () => {
        this.snackBar.open('Étudiant supprimé avec succès', 'Fermer', {
          duration: 3000
        });
        this.loadStudents();
      },
      error: () => {
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', {
          duration: 5000
        });
      }
    });
  }

  goToAddStudent = () => {
    this.router.navigate(['/dashboard/students/new']);
  };
}