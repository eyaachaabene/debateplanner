import { Component, Input } from '@angular/core';
import { DefenseStatus, Mention } from '@core/models';
import { DefenseStatusLabelPipe } from '../../pipes/defense-status-label.pipe';
import { MentionLabelPipe } from '../../pipes/mention-label.pipe';

@Component({
  selector: 'app-status-badge',
  standalone: true,
  imports: [DefenseStatusLabelPipe, MentionLabelPipe],
  template: `
    @if (type === 'status') {
      @if (status) {
        <span class="badge" [class]="getStatusClass()">
          {{ status | defenseStatusLabel }}
        </span>
      }
    } @else {
      @if (mention) {
        <span class="badge" [class]="getMentionClass()">
          {{ mention | mentionLabel }}
        </span>
      }
    }
  `,
  styles: [`
    .badge {
      padding: 4px 12px;
      border-radius: 16px;
      font-size: 12px;
      font-weight: 500;
      display: inline-block;
    }
    
    .planned {
      background-color: #fff3e0;
      color: #e65100;
    }
    
    .ongoing {
      background-color: #e8f5e9;
      color: #2e7d32;
    }
    
    .published {
      background-color: #e3f2fd;
      color: #1565c0;
    }

    .passable {
      background-color: #fff3e0;
      color: #e65100;
    }
    
    .fail {
      background-color: #ffebee;
      color: #c62828;
    }

    .fairly-good {
      background-color: #fff8e1;
      color: #f57f17;
    }

    .good {
      background-color: #e8f5e9;
      color: #2e7d32;
    }

    .very-good {
      background-color: #e3f2fd;
      color: #1565c0;
    }

    .excellent {
      background-color: #ede7f6;
      color: #5e35b1;
    }
  `]
})
export class StatusBadgeComponent {
  @Input() type: 'status' | 'mention' = 'status';
  @Input() status?: DefenseStatus;
  @Input() mention?: Mention | string;

  getStatusClass(): string {
    if (!this.status) return '';
    return this.status.toLowerCase();
  }

  getMentionClass(): string {
    if (!this.mention) return '';
    return this.mention.toLowerCase().replace('_', '-');
  }
}
