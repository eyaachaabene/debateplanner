import {
  Component,
  OnInit,
  inject,
  signal,
  computed
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import { DefenseService, StudentService } from '@core/services';
import { ReportMetadata } from '@core/models';
import { LoadingSpinnerComponent, EmptyStateComponent } from '@shared/components';
import { HttpEventType } from '@angular/common/http';

type UploadState = 'idle' | 'uploading' | 'done' | 'error';

@Component({
  selector: 'app-upload-report',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatIconModule,
    MatButtonModule,
    MatProgressBarModule,
    MatSnackBarModule,
    MatDividerModule,
    LoadingSpinnerComponent,
    EmptyStateComponent
  ],
  template: `
    <div class="upload-report">
      <h1>Rapport de soutenance</h1>
      <p class="subtitle">
        Déposez votre rapport avant la soutenance. Formats acceptés : PDF, Word (.doc, .docx). Taille max : 50 Mo.
      </p>

      @if (isLoading()) {
        <app-loading-spinner message="Chargement..."></app-loading-spinner>
      } @else if (noDefense()) {
        <app-empty-state
          icon="event_busy"
          title="Aucune soutenance planifiée"
          message="Vous pourrez déposer votre rapport une fois votre soutenance planifiée."
        ></app-empty-state>
      } @else {

        <!-- Existing report card -->
        @if (existingReport()) {
          <mat-card class="report-card existing">
            <mat-card-content>
              <div class="report-info">
                <div class="file-icon">
                  <mat-icon>description</mat-icon>
                </div>
                <div class="file-details">
                  <span class="filename">{{ existingReport()!.originalFilename }}</span>
                  <span class="meta">
                    {{ formatSize(existingReport()!.fileSize) }} &bull;
                    Déposé le {{ existingReport()!.uploadedAt | date:'dd/MM/yyyy à HH:mm' }}
                  </span>
                </div>
                <div class="actions">
                  <button mat-icon-button color="primary" (click)="download()" title="Télécharger">
                    <mat-icon>download</mat-icon>
                  </button>
                  <button mat-icon-button color="warn" (click)="deleteReport()" title="Supprimer">
                    <mat-icon>delete</mat-icon>
                  </button>
                </div>
              </div>
            </mat-card-content>
          </mat-card>

          <mat-divider class="section-divider"></mat-divider>
          <p class="replace-hint">
            <mat-icon>info</mat-icon>
            Vous pouvez remplacer votre rapport en déposant un nouveau fichier ci-dessous.
          </p>
        }

        <!-- Drop zone -->
        <div
          class="drop-zone"
          [class.dragover]="isDragging()"
          [class.has-file]="selectedFile()"
          (dragover)="onDragOver($event)"
          (dragleave)="onDragLeave()"
          (drop)="onDrop($event)"
          (click)="fileInput.click()"
        >
          <input
            #fileInput
            type="file"
            accept=".pdf,.doc,.docx"
            style="display:none"
            (change)="onFileSelected($event)"
          />

          @if (!selectedFile()) {
            <mat-icon class="drop-icon">cloud_upload</mat-icon>
            <p class="drop-text">Glissez votre fichier ici ou <span class="link">parcourir</span></p>
            <p class="drop-hint">PDF · DOC · DOCX — max 50 Mo</p>
          } @else {
            <mat-icon class="drop-icon selected">check_circle</mat-icon>
            <p class="drop-text selected">{{ selectedFile()!.name }}</p>
            <p class="drop-hint">{{ formatSize(selectedFile()!.size) }}</p>
          }
        </div>

        @if (validationError()) {
          <p class="validation-error">
            <mat-icon>error</mat-icon>
            {{ validationError() }}
          </p>
        }

        <!-- Progress bar -->
        @if (uploadState() === 'uploading') {
          <mat-progress-bar
            mode="determinate"
            [value]="uploadProgress()"
            class="progress-bar"
          ></mat-progress-bar>
          <p class="progress-label">{{ uploadProgress() }}%</p>
        }

        <!-- Submit button -->
        <div class="submit-row">
          @if (selectedFile()) {
            <button
              mat-button
              color="basic"
              (click)="clearSelection()"
              [disabled]="uploadState() === 'uploading'"
            >
              Annuler
            </button>
          }
          <button
            mat-raised-button
            color="primary"
            [disabled]="!selectedFile() || uploadState() === 'uploading' || !!validationError()"
            (click)="uploadReport()"
          >
            @if (uploadState() === 'uploading') {
              Envoi en cours...
            } @else if (existingReport()) {
              Remplacer le rapport
            } @else {
              Déposer le rapport
            }
          </button>
        </div>
      }
    </div>
  `,
  styles: [`
    .upload-report {
      max-width: 760px;
      margin: 0 auto;
    }

    h1 {
      font-size: 24px;
      font-weight: 500;
      margin-bottom: 8px;
    }

    .subtitle {
      color: #666;
      margin-bottom: 28px;
      line-height: 1.6;
    }

    /* Existing report */
    .report-card.existing {
      margin-bottom: 8px;
      border-left: 4px solid #3f51b5;
    }

    .report-info {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .file-icon mat-icon {
      font-size: 40px;
      width: 40px;
      height: 40px;
      color: #3f51b5;
    }

    .file-details {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .filename {
      font-weight: 500;
      font-size: 15px;
    }

    .meta {
      font-size: 12px;
      color: #888;
    }

    .actions {
      display: flex;
      gap: 4px;
    }

    .section-divider {
      margin: 20px 0 16px;
    }

    .replace-hint {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: #888;
      margin-bottom: 16px;
    }

    .replace-hint mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    /* Drop zone */
    .drop-zone {
      border: 2px dashed #bbb;
      border-radius: 12px;
      padding: 48px 32px;
      text-align: center;
      cursor: pointer;
      transition: border-color 0.2s, background 0.2s;
      background: #fafafa;
    }

    .drop-zone:hover,
    .drop-zone.dragover {
      border-color: #3f51b5;
      background: #f0f4ff;
    }

    .drop-zone.has-file {
      border-color: #4caf50;
      background: #f1faf2;
    }

    .drop-icon {
      font-size: 52px;
      width: 52px;
      height: 52px;
      color: #bbb;
      margin-bottom: 12px;
    }

    .drop-icon.selected {
      color: #4caf50;
    }

    .drop-text {
      font-size: 16px;
      margin: 0 0 4px;
      color: #444;
    }

    .drop-text.selected {
      font-weight: 500;
      color: #2e7d32;
    }

    .drop-text .link {
      color: #3f51b5;
      text-decoration: underline;
    }

    .drop-hint {
      font-size: 13px;
      color: #999;
      margin: 0;
    }

    /* Validation error */
    .validation-error {
      display: flex;
      align-items: center;
      gap: 6px;
      color: #d32f2f;
      font-size: 13px;
      margin-top: 10px;
    }

    .validation-error mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    /* Progress */
    .progress-bar {
      margin-top: 16px;
      border-radius: 4px;
    }

    .progress-label {
      text-align: center;
      font-size: 13px;
      color: #666;
      margin: 6px 0 0;
    }

    /* Submit row */
    .submit-row {
      display: flex;
      justify-content: flex-end;
      gap: 12px;
      margin-top: 24px;
    }
  `]
})
export class UploadReportComponent implements OnInit {
  private readonly defenseService = inject(DefenseService);
  private readonly studentService = inject(StudentService);
  private readonly snackBar = inject(MatSnackBar);

  // State
  isLoading = signal(true);
  noDefense = signal(false);
  defenseId = signal<number | null>(null);
  existingReport = signal<ReportMetadata | null>(null);
  selectedFile = signal<File | null>(null);
  isDragging = signal(false);
  validationError = signal<string | null>(null);
  uploadState = signal<UploadState>('idle');
  uploadProgress = signal(0);

  private readonly ALLOWED_TYPES = [
    'application/pdf',
    'application/msword',
    'application/vnd.openxmlformats-officedocument.wordprocessingml.document'
  ];
  private readonly MAX_SIZE_BYTES = 50 * 1024 * 1024;

  ngOnInit(): void {
    this.studentService.getMe().subscribe({
      next: (student) => {
        this.defenseService.getMyResult().subscribe({
          next: (defense) => {
            this.defenseId.set(defense.id);
            this.isLoading.set(false);
            this.loadExistingReport(defense.id);
          },
          error: () => {
            this.noDefense.set(true);
            this.isLoading.set(false);
          }
        });
      },
      error: () => {
        this.isLoading.set(false);
        this.snackBar.open('Erreur lors du chargement du profil étudiant', 'Fermer', { duration: 4000 });
      }
    });
  }

  private loadExistingReport(defenseId: number): void {
    this.defenseService.getReportMetadata(defenseId).subscribe({
      next: (report) => this.existingReport.set(report),
      error: () => this.existingReport.set(null) // 404 means no report yet — normal
    });
  }

  // ── Drag & drop ────────────────────────────────────────────────────────────

  onDragOver(event: DragEvent): void {
    event.preventDefault();
    this.isDragging.set(true);
  }

  onDragLeave(): void {
    this.isDragging.set(false);
  }

  onDrop(event: DragEvent): void {
    event.preventDefault();
    this.isDragging.set(false);
    const file = event.dataTransfer?.files[0];
    if (file) this.setFile(file);
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (file) this.setFile(file);
    input.value = ''; // reset so same file can be re-selected after clearing
  }

  private setFile(file: File): void {
    this.validationError.set(null);
    if (!this.ALLOWED_TYPES.includes(file.type)) {
      this.validationError.set('Type de fichier non accepté. Utilisez un PDF ou un document Word.');
      return;
    }
    if (file.size > this.MAX_SIZE_BYTES) {
      this.validationError.set('Le fichier dépasse la limite de 50 Mo.');
      return;
    }
    this.selectedFile.set(file);
    this.uploadState.set('idle');
    this.uploadProgress.set(0);
  }

  clearSelection(): void {
    this.selectedFile.set(null);
    this.validationError.set(null);
    this.uploadState.set('idle');
    this.uploadProgress.set(0);
  }

  // ── Upload ─────────────────────────────────────────────────────────────────

  uploadReport(): void {
    const file = this.selectedFile();
    const defenseId = this.defenseId();
    if (!file || !defenseId) return;

    this.uploadState.set('uploading');
    this.uploadProgress.set(0);

    this.defenseService.uploadReport(defenseId, file).subscribe({
      next: (report) => {
        this.existingReport.set(report);
        this.clearSelection();
        this.uploadState.set('done');
        this.snackBar.open('Rapport déposé avec succès.', 'Fermer', { duration: 4000 });
      },
      error: (err) => {
        this.uploadState.set('error');
        const msg = err?.error?.message ?? 'Une erreur est survenue lors du dépôt.';
        this.snackBar.open(msg, 'Fermer', { duration: 6000 });
      }
    });
  }

  // ── Download ───────────────────────────────────────────────────────────────

  download(): void {
    const defenseId = this.defenseId();
    const report = this.existingReport();
    if (!defenseId || !report) return;

    this.defenseService.downloadReport(defenseId).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = report.originalFilename;
        a.click();
        URL.revokeObjectURL(url);
      },
      error: () => {
        this.snackBar.open('Impossible de télécharger le fichier.', 'Fermer', { duration: 4000 });
      }
    });
  }

  // ── Delete ─────────────────────────────────────────────────────────────────

  deleteReport(): void {
    const defenseId = this.defenseId();
    if (!defenseId) return;

    if (!confirm('Supprimer le rapport déposé ?')) return;

    this.defenseService.deleteReport(defenseId).subscribe({
      next: () => {
        this.existingReport.set(null);
        this.snackBar.open('Rapport supprimé.', 'Fermer', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Erreur lors de la suppression.', 'Fermer', { duration: 4000 });
      }
    });
  }

  // ── Utility ────────────────────────────────────────────────────────────────

  formatSize(bytes: number): string {
    if (bytes < 1024) return `${bytes} o`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} Ko`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} Mo`;
  }
}
