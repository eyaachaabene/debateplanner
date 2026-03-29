import { Pipe, PipeTransform } from '@angular/core';
import { DefenseStatus } from '@core/models';

@Pipe({
  name: 'defenseStatusLabel',
  standalone: true
})
export class DefenseStatusLabelPipe implements PipeTransform {
  private readonly labels: Record<DefenseStatus, string> = {
    [DefenseStatus.PENDING]: 'En attente',
    [DefenseStatus.CONFIRMED]: 'Confirmée',
    [DefenseStatus.COMPLETED]: 'Terminée',
    [DefenseStatus.CANCELLED]: 'Annulée',
    [DefenseStatus.PLANNED]: 'Planifiée'
  };

  transform(value: DefenseStatus): string {
    return this.labels[value] || value;
  }
}
