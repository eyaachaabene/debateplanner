import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { RoomService } from '@core/services';
import { LoadingSpinnerComponent } from '@shared/components';

@Component({
  selector: 'app-room-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="room-form">
      <div class="page-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h1>{{ isEditMode ? 'Modifier la salle' : 'Ajouter une salle' }}</h1>
      </div>

      @if (isLoadingRoom()) {
        <app-loading-spinner message="Chargement..."></app-loading-spinner>
      } @else {
        <mat-card>
          <mat-card-content>
            <form [formGroup]="roomForm" (ngSubmit)="onSubmit()">
              <div class="form-row">
                <mat-form-field appearance="outline">
                  <mat-label>Nom de la salle</mat-label>
                  <input matInput formControlName="name" placeholder="Ex: Salle A101">
                  @if (roomForm.get('name')?.hasError('required')) {
                    <mat-error>Le nom est requis</mat-error>
                  }
                </mat-form-field>

                <mat-form-field appearance="outline">
                  <mat-label>Bâtiment</mat-label>
                  <input matInput formControlName="building" placeholder="Ex: Bâtiment A">
                  @if (roomForm.get('building')?.hasError('required')) {
                    <mat-error>Le bâtiment est requis</mat-error>
                  }
                </mat-form-field>
              </div>

              <mat-form-field appearance="outline" class="capacity-field">
                <mat-label>Capacité</mat-label>
                <input matInput formControlName="capacity" type="number" min="1">
                <span matSuffix>places</span>
                @if (roomForm.get('capacity')?.hasError('required')) {
                  <mat-error>La capacité est requise</mat-error>
                }
                @if (roomForm.get('capacity')?.hasError('min')) {
                  <mat-error>La capacité doit être supérieure à 0</mat-error>
                }
              </mat-form-field>

              <div class="equipment-section">
                <h3>Équipements disponibles</h3>
                <div class="checkboxes">
                  <mat-checkbox formControlName="hasProjector" color="primary">
                    <mat-icon>videocam</mat-icon>
                    Projecteur
                  </mat-checkbox>
                  <mat-checkbox formControlName="hasVideoConference" color="primary">
                    <mat-icon>video_call</mat-icon>
                    Visioconférence
                  </mat-checkbox>
                </div>
              </div>

              @if (isEditMode) {
                <mat-checkbox formControlName="isAvailable" color="primary">
                  Salle disponible pour réservation
                </mat-checkbox>
              }

              <div class="actions">
                <button mat-button type="button" (click)="goBack()">Annuler</button>
                <button 
                  mat-flat-button 
                  color="primary" 
                  type="submit"
                  [disabled]="roomForm.invalid || isSubmitting()"
                >
                  @if (isSubmitting()) {
                    <mat-spinner diameter="20"></mat-spinner>
                  } @else {
                    {{ isEditMode ? 'Mettre à jour' : 'Créer' }}
                  }
                </button>
              </div>
            </form>
          </mat-card-content>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .room-form {
      max-width: 600px;
      margin: 0 auto;
    }
    
    .page-header {
      display: flex;
      align-items: center;
      gap: 16px;
      margin-bottom: 24px;
      
      h1 {
        font-size: 24px;
        font-weight: 500;
        margin: 0;
      }
    }
    
    .form-row {
      display: flex;
      gap: 16px;
      
      mat-form-field {
        flex: 1;
      }
    }
    
    .capacity-field {
      width: 200px;
    }
    
    mat-form-field {
      margin-bottom: 8px;
    }
    
    .equipment-section {
      margin: 16px 0;
      
      h3 {
        font-size: 14px;
        font-weight: 500;
        color: #666;
        margin-bottom: 12px;
      }
      
      .checkboxes {
        display: flex;
        gap: 24px;
        
        mat-checkbox {
          mat-icon {
            font-size: 18px;
            width: 18px;
            height: 18px;
            margin-right: 4px;
            vertical-align: middle;
          }
        }
      }
    }
    
    mat-checkbox {
      margin-bottom: 16px;
    }
    
    .actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      margin-top: 24px;
    }
  `]
})
export class RoomFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private roomService = inject(RoomService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private snackBar = inject(MatSnackBar);

  roomForm: FormGroup = this.fb.group({
    name: ['', Validators.required],
    building: ['', Validators.required],
    capacity: [30, [Validators.required, Validators.min(1)]],
    hasProjector: [true],
    hasVideoConference: [false],
    isAvailable: [true]
  });

  isEditMode = false;
  roomId: number | null = null;
  isLoadingRoom = signal(false);
  isSubmitting = signal(false);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.roomId = +id;
      this.loadRoom();
    }
  }

  private loadRoom(): void {
    if (!this.roomId) return;
    
    this.isLoadingRoom.set(true);
    this.roomService.getById(this.roomId).subscribe({
      next: (room) => {
        this.roomForm.patchValue({
          name: room.name,
          capacity: room.capacity
        });
        this.isLoadingRoom.set(false);
      },
      error: () => {
        this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 5000 });
        this.goBack();
      }
    });
  }

  onSubmit(): void {
    if (this.roomForm.invalid) return;

    this.isSubmitting.set(true);
    const formData = this.roomForm.value;

    const request$ = this.isEditMode
      ? this.roomService.update(this.roomId!, formData)
      : this.roomService.create(formData);

    request$.subscribe({
      next: () => {
        this.snackBar.open(
          this.isEditMode ? 'Salle mise à jour' : 'Salle créée',
          'Fermer',
          { duration: 3000 }
        );
        this.goBack();
      },
      error: (error) => {
        this.isSubmitting.set(false);
        this.snackBar.open(
          error.error?.message || 'Une erreur est survenue',
          'Fermer',
          { duration: 5000 }
        );
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/dashboard/rooms']);
  }
}
