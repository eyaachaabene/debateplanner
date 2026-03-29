import { Component, Input } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-empty-state',
  standalone: true,
  imports: [MatIconModule, MatButtonModule],
  template: `
    <div class="empty-state">
      <mat-icon>{{ icon }}</mat-icon>
      <h3>{{ title }}</h3>
      <p>{{ message }}</p>
      @if (actionLabel) {
        <button mat-flat-button color="primary" (click)="onAction()">
          {{ actionLabel }}
        </button>
      }
    </div>
  `,
  styles: [`
    .empty-state {
      text-align: center;
      padding: 48px 24px;
      color: #666;
      
      mat-icon {
        font-size: 64px;
        width: 64px;
        height: 64px;
        color: #ccc;
        margin-bottom: 16px;
      }
      
      h3 {
        font-size: 18px;
        margin-bottom: 8px;
        color: #333;
      }
      
      p {
        font-size: 14px;
        margin-bottom: 24px;
      }
    }
  `]
})
export class EmptyStateComponent {
  @Input() icon = 'inbox';
  @Input() title = 'Aucune donnée';
  @Input() message = 'Il n\'y a rien à afficher pour le moment.';
  @Input() actionLabel = '';
  @Input() action?: () => void;

  onAction(): void {
    if (this.action) {
      this.action();
    }
  }
}
