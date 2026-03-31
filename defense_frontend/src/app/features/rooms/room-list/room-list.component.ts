import { Component, inject, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatTableModule, MatTableDataSource } from '@angular/material/table';
import { MatPaginatorModule, MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSortModule, MatSort } from '@angular/material/sort';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RoomService } from '@core/services';
import { Room } from '@core/models';
import { ConfirmDialogComponent, LoadingSpinnerComponent, EmptyStateComponent } from '@shared/components';

@Component({
  selector: 'app-room-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterModule,
    FormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatSortModule,
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
    <div class="room-list">
      <div class="page-header">
        <h1>Gestion des salles</h1>
        <button mat-flat-button color="primary" routerLink="/dashboard/rooms/new">
          <mat-icon>add</mat-icon>
          Ajouter une salle
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
                placeholder="Nom de la salle"
              />
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>
          </div>

          @if (isLoading()) {
            <app-loading-spinner message="Chargement des salles..."></app-loading-spinner>
          } @else if (dataSource.data.length === 0) {
            <app-empty-state
              icon="meeting_room"
              title="Aucune salle"
              message="Il n'y a pas encore de salle enregistrée."
              actionLabel="Ajouter une salle"
              [action]="goToAddRoom"
            ></app-empty-state>
          } @else {
            <table mat-table [dataSource]="dataSource" matSort>
              <ng-container matColumnDef="name">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Nom de salle</th>
                <td mat-cell *matCellDef="let room">{{ room.name }}</td>
              </ng-container>

              <ng-container matColumnDef="capacity">
                <th mat-header-cell *matHeaderCellDef mat-sort-header>Capacité</th>
                <td mat-cell *matCellDef="let room">{{ room.capacity }}</td>
              </ng-container>

              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Actions</th>
                <td mat-cell *matCellDef="let room">
                  <button
                    mat-icon-button
                    color="primary"
                    [routerLink]="['/dashboard/rooms', room.id, 'edit']"
                    matTooltip="Modifier"
                  >
                    <mat-icon>edit</mat-icon>
                  </button>

                  <button
                    mat-icon-button
                    color="warn"
                    (click)="confirmDelete(room)"
                    matTooltip="Supprimer"
                  >
                    <mat-icon>delete</mat-icon>
                  </button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns"></tr>
            </table>

            <mat-paginator
              [length]="totalElements"
              [pageSize]="pageSize"
              [pageSizeOptions]="[10, 25, 50]"
              (page)="onPageChange($event)"
              showFirstLastButtons
            ></mat-paginator>
          }
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .room-list {
      .page-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;

        h1 {
          font-size: 24px;
          font-weight: 500;
          margin: 0;
        }
      }
    }

    .filters {
      margin-bottom: 16px;

      mat-form-field {
        width: 100%;
        max-width: 400px;
      }
    }

    table {
      width: 100%;
    }
  `]
})
export class RoomListComponent implements OnInit {
  private readonly roomService = inject(RoomService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly router = inject(Router);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  dataSource = new MatTableDataSource<Room>([]);
  displayedColumns: string[] = ['name', 'capacity', 'actions'];

  isLoading = signal(false);
  searchQuery = '';
  totalElements = 0;
  pageSize = 10;
  currentPage = 0;

  ngOnInit(): void {
    this.loadRooms();
  }

  loadRooms(): void {
    this.isLoading.set(true);

    this.roomService.getAll({
      search: this.searchQuery || undefined,
      page: this.currentPage,
      size: this.pageSize
    }).subscribe({
      next: (rooms) => {
        this.dataSource.data = rooms;
        this.totalElements = rooms.length;
        this.isLoading.set(false);
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement des salles', 'Fermer', {
          duration: 5000
        });
      }
    });
  }

  applyFilter(): void {
    this.currentPage = 0;
    this.loadRooms();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadRooms();
  }

  confirmDelete(room: Room): void {
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      data: {
        title: 'Supprimer la salle',
        message: `Êtes-vous sûr de vouloir supprimer la salle ${room.name} ?`
      }
    });

    dialogRef.afterClosed().subscribe((confirmed: boolean) => {
      if (confirmed) {
        this.deleteRoom(room.id);
      }
    });
  }

  private deleteRoom(id: number): void {
    this.roomService.delete(id).subscribe({
      next: () => {
        this.snackBar.open('Salle supprimée avec succès', 'Fermer', {
          duration: 3000
        });
        this.loadRooms();
      },
      error: () => {
        this.snackBar.open('Erreur lors de la suppression', 'Fermer', {
          duration: 5000
        });
      }
    });
  }

  goToAddRoom = () => {
    this.router.navigate(['/dashboard/rooms/new']);
  };
}