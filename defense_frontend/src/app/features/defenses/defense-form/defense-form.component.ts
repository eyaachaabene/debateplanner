import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatStepperModule } from '@angular/material/stepper';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { DefenseService, StudentService, ProfessorService, RoomService } from '@core/services';
import { Student, Professor, Room, Defense } from '@core/models';
import { MajorLabelPipe } from '@shared/pipes';
import { LoadingSpinnerComponent } from '@shared/components';

@Component({
  selector: 'app-defense-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatIconModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatStepperModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MajorLabelPipe,
    LoadingSpinnerComponent
  ],
  template: `
    <div class="defense-form">
      <div class="page-header">
        <button mat-icon-button (click)="goBack()">
          <mat-icon>arrow_back</mat-icon>
        </button>
        <h1>{{ isEditMode ? 'Modifier la soutenance' : 'Planifier une soutenance' }}</h1>
      </div>

      @if (isLoadingDefense()) {
        <app-loading-spinner message="Chargement..."></app-loading-spinner>
      } @else {
        <mat-card>
          <mat-card-content>
            <mat-stepper [linear]="true">
              <mat-step [stepControl]="planningForm">
                <ng-template matStepLabel>Planification</ng-template>

                <form [formGroup]="planningForm">
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Projet</mat-label>
                    <input matInput formControlName="projectTitle" />
                    @if (planningForm.get('projectTitle')?.hasError('required')) {
                      <mat-error>Le projet est requis</mat-error>
                    }
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Étudiant</mat-label>
                    <mat-select formControlName="studentId">
                      @for (student of students(); track student.id) {
                        <mat-option [value]="student.id">
                          {{ student.firstName }} {{ student.lastName }} - {{ student.major | majorLabel }}
                        </mat-option>
                      }
                    </mat-select>
                    @if (planningForm.get('studentId')?.hasError('required')) {
                      <mat-error>L'étudiant est requis</mat-error>
                    }
                  </mat-form-field>

                  <div class="form-row">
                    <mat-form-field appearance="outline">
                      <mat-label>Date de soutenance</mat-label>
                      <input
                        matInput
                        [matDatepicker]="picker"
                        formControlName="defenseDate"
                        [min]="minDate"
                        (dateChange)="onSlotChanged()"
                      />
                      <mat-datepicker-toggle matIconSuffix [for]="picker"></mat-datepicker-toggle>
                      <mat-datepicker #picker></mat-datepicker>
                    </mat-form-field>

                    <mat-form-field appearance="outline">
                      <mat-label>Heure de début</mat-label>
                      <mat-select formControlName="startTime" (selectionChange)="onSlotChanged()">
                        @for (time of timeSlots; track time) {
                          <mat-option [value]="time">{{ time }}</mat-option>
                        }
                      </mat-select>
                    </mat-form-field>

                    <mat-form-field appearance="outline">
                      <mat-label>Heure de fin</mat-label>
                      <mat-select formControlName="endTime" (selectionChange)="onSlotChanged()">
                        @for (time of timeSlots; track time) {
                          <mat-option [value]="time">{{ time }}</mat-option>
                        }
                      </mat-select>
                    </mat-form-field>
                  </div>

                  <div class="step-actions">
                    <button mat-flat-button color="primary" matStepperNext [disabled]="planningForm.invalid">
                      Suivant
                    </button>
                  </div>
                </form>
              </mat-step>

              <mat-step [stepControl]="assignmentForm">
                <ng-template matStepLabel>Affectations</ng-template>

                <form [formGroup]="assignmentForm">
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Salle</mat-label>
                    <mat-select formControlName="roomId" [disabled]="!slotReady()">
                      @for (room of availableRooms(); track room.id) {
                        <mat-option [value]="room.id">
                          {{ room.name }} ({{ room.capacity }} places)
                        </mat-option>
                      }
                    </mat-select>
                    @if (assignmentForm.get('roomId')?.hasError('required')) {
                      <mat-error>La salle est requise</mat-error>
                    }
                  </mat-form-field>

                  <div class="form-row">
                    <mat-form-field appearance="outline">
                      <mat-label>Encadrant</mat-label>
                      <mat-select formControlName="supervisorId" [disabled]="!slotReady()">
                        @for (professor of availableSupervisors(); track professor.id) {
                          <mat-option [value]="professor.id">
                            {{ professor.firstName }} {{ professor.lastName }}
                          </mat-option>
                        }
                      </mat-select>
                    </mat-form-field>

                    <mat-form-field appearance="outline">
                      <mat-label>Président</mat-label>
                      <mat-select formControlName="presidentId" [disabled]="!slotReady()">
                        @for (professor of availablePresidents(); track professor.id) {
                          <mat-option [value]="professor.id">
                            {{ professor.firstName }} {{ professor.lastName }}
                          </mat-option>
                        }
                      </mat-select>
                    </mat-form-field>
                  </div>

                  <div class="form-row">
                    <mat-form-field appearance="outline">
                      <mat-label>Rapporteur</mat-label>
                      <mat-select formControlName="reviewerId" [disabled]="!slotReady()">
                        @for (professor of availableReviewers(); track professor.id) {
                          <mat-option [value]="professor.id">
                            {{ professor.firstName }} {{ professor.lastName }}
                          </mat-option>
                        }
                      </mat-select>
                    </mat-form-field>

                    <mat-form-field appearance="outline">
                      <mat-label>Examinateur</mat-label>
                      <mat-select formControlName="examinerId" [disabled]="!slotReady()">
                        @for (professor of availableExaminers(); track professor.id) {
                          <mat-option [value]="professor.id">
                            {{ professor.firstName }} {{ professor.lastName }}
                          </mat-option>
                        }
                      </mat-select>
                    </mat-form-field>
                  </div>

                  <div class="step-actions">
                    <button mat-button matStepperPrevious>Précédent</button>
                    <button mat-flat-button color="primary" matStepperNext [disabled]="assignmentForm.invalid">
                      Suivant
                    </button>
                  </div>
                </form>
              </mat-step>

              <mat-step>
                <ng-template matStepLabel>Confirmation</ng-template>

                <div class="confirmation-block">
                  <p>Vérifiez les informations puis enregistrez la soutenance.</p>
                </div>

                <div class="step-actions">
                  <button mat-button matStepperPrevious>Précédent</button>
                  <button
                    mat-flat-button
                    color="primary"
                    (click)="onSubmit()"
                    [disabled]="isSubmitting()"
                  >
                    @if (isSubmitting()) {
                      <mat-spinner diameter="20"></mat-spinner>
                    } @else {
                      {{ isEditMode ? 'Mettre à jour' : 'Créer' }}
                    }
                  </button>
                </div>
              </mat-step>
            </mat-stepper>
          </mat-card-content>
        </mat-card>
      }
    </div>
  `,
  styles: [`
    .defense-form {
      max-width: 900px;
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

    .form-row {
      display: flex;
      gap: 16px;
    }

    .form-row mat-form-field {
      flex: 1;
    }

    .full-width {
      width: 100%;
    }

    mat-form-field {
      margin-bottom: 8px;
    }

    .step-actions {
      display: flex;
      justify-content: flex-end;
      gap: 8px;
      margin-top: 24px;
    }

    .confirmation-block {
      padding: 16px 0;
    }
  `]
})
export class DefenseFormComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly defenseService = inject(DefenseService);
  private readonly studentService = inject(StudentService);
  private readonly professorService = inject(ProfessorService);
  private readonly roomService = inject(RoomService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly snackBar = inject(MatSnackBar);

  planningForm: FormGroup = this.fb.group({
    projectTitle: ['', Validators.required],
    studentId: [null, Validators.required],
    defenseDate: [null, Validators.required],
    startTime: ['', Validators.required],
    endTime: ['', Validators.required]
  });

  assignmentForm: FormGroup = this.fb.group({
    roomId: [null, Validators.required],
    supervisorId: [null, Validators.required],
    presidentId: [null, Validators.required],
    reviewerId: [null, Validators.required],
    examinerId: [null, Validators.required]
  });

  students = signal<Student[]>([]);
  availableRooms = signal<Room[]>([]);
  availableSupervisors = signal<Professor[]>([]);
  availablePresidents = signal<Professor[]>([]);
  availableReviewers = signal<Professor[]>([]);
  availableExaminers = signal<Professor[]>([]);

  minDate = new Date();
  timeSlots = ['08:00', '09:00', '10:00', '11:00', '14:00', '15:00', '16:00', '17:00'];

  isEditMode = false;
  defenseId: number | null = null;
  isLoadingDefense = signal(false);
  isSubmitting = signal(false);
  slotReady = signal(false);

  ngOnInit(): void {
    this.loadStudents();

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.defenseId = +id;
      this.loadDefense();
    }
  }

  private loadStudents(): void {
    this.studentService.getAll().subscribe({
      next: (students) => this.students.set(students)
    });
  }

  private loadDefense(): void {
    if (!this.defenseId) return;

    this.isLoadingDefense.set(true);

    this.defenseService.getById(this.defenseId).subscribe({
      next: (defense: Defense) => {
        this.planningForm.patchValue({
          projectTitle: defense.projectTitle,
          studentId: defense.studentId,
          defenseDate: new Date(defense.defenseDate),
          startTime: defense.startTime,
          endTime: defense.endTime
        });

        this.assignmentForm.patchValue({
          roomId: defense.roomId,
          supervisorId: defense.supervisorId,
          presidentId: defense.presidentId,
          reviewerId: defense.reviewerId,
          examinerId: defense.examinerId
        });

        this.slotReady.set(true);
        this.loadSlotDependencies();
        this.isLoadingDefense.set(false);
      },
      error: () => {
        this.isLoadingDefense.set(false);
        this.snackBar.open('Erreur lors du chargement', 'Fermer', { duration: 5000 });
        this.goBack();
      }
    });
  }

  onSlotChanged(): void {
    const date = this.planningForm.get('defenseDate')?.value;
    const startTime = this.planningForm.get('startTime')?.value;
    const endTime = this.planningForm.get('endTime')?.value;

    const ready = !!date && !!startTime && !!endTime;
    this.slotReady.set(ready);

    if (ready) {
      this.loadSlotDependencies();
    } else {
      this.availableRooms.set([]);
      this.availableSupervisors.set([]);
      this.availablePresidents.set([]);
      this.availableReviewers.set([]);
      this.availableExaminers.set([]);
    }
  }

  private loadSlotDependencies(): void {
    const rawDate = this.planningForm.get('defenseDate')?.value;
    const startTime = this.planningForm.get('startTime')?.value;
    const endTime = this.planningForm.get('endTime')?.value;

    if (!rawDate || !startTime || !endTime) return;

    const defenseDate = rawDate instanceof Date
      ? rawDate.toISOString().split('T')[0]
      : rawDate;

    const excludeDefenseId = this.isEditMode ? this.defenseId! : undefined;

    this.roomService.getAvailable(defenseDate, startTime, endTime, excludeDefenseId).subscribe({
      next: (rooms) => this.availableRooms.set(rooms)
    });

    this.professorService.getAvailableForRole('SUPERVISOR', defenseDate, startTime, endTime, excludeDefenseId).subscribe({
      next: (professors) => this.availableSupervisors.set(professors)
    });

    this.professorService.getAvailableForRole('PRESIDENT', defenseDate, startTime, endTime, excludeDefenseId).subscribe({
      next: (professors) => this.availablePresidents.set(professors)
    });

    this.professorService.getAvailableForRole('REVIEWER', defenseDate, startTime, endTime, excludeDefenseId).subscribe({
      next: (professors) => this.availableReviewers.set(professors)
    });

    this.professorService.getAvailableForRole('EXAMINER', defenseDate, startTime, endTime, excludeDefenseId).subscribe({
      next: (professors) => this.availableExaminers.set(professors)
    });
  }

  onSubmit(): void {
    if (this.planningForm.invalid || this.assignmentForm.invalid) {
      this.planningForm.markAllAsTouched();
      this.assignmentForm.markAllAsTouched();
      this.snackBar.open('Veuillez remplir tous les champs requis', 'Fermer', { duration: 5000 });
      return;
    }

    const formData = {
      projectTitle: this.planningForm.get('projectTitle')?.value,
      studentId: this.planningForm.get('studentId')?.value,
      defenseDate: this.formatDate(this.planningForm.get('defenseDate')?.value),
      startTime: this.planningForm.get('startTime')?.value,
      endTime: this.planningForm.get('endTime')?.value,
      roomId: this.assignmentForm.get('roomId')?.value,
      supervisorId: this.assignmentForm.get('supervisorId')?.value,
      presidentId: this.assignmentForm.get('presidentId')?.value,
      reviewerId: this.assignmentForm.get('reviewerId')?.value,
      examinerId: this.assignmentForm.get('examinerId')?.value
    };

    this.isSubmitting.set(true);

    const request$ = this.isEditMode
      ? this.defenseService.update(this.defenseId!, formData)
      : this.defenseService.create(formData);

    request$.subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.snackBar.open(
          this.isEditMode ? 'Soutenance mise à jour' : 'Soutenance créée',
          'Fermer',
          { duration: 3000 }
        );
        this.goBack();
      },
      error: (error) => {
        this.isSubmitting.set(false);
        this.snackBar.open(
          error?.error?.message || 'Une erreur est survenue',
          'Fermer',
          { duration: 5000 }
        );
      }
    });
  }

  private formatDate(date: Date | string): string {
    if (date instanceof Date) {
      return date.toISOString().split('T')[0];
    }
    return date;
  }

  goBack(): void {
    this.router.navigate(['/dashboard/defenses']);
  }
}