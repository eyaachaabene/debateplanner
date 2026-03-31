import { Pipe, PipeTransform } from '@angular/core';
import { DefenseStatus } from '@core/models';

@Pipe({
  name: 'defenseStatusLabel',
  standalone: true
})
export class DefenseStatusLabelPipe implements PipeTransform {
  private readonly labels: Record<DefenseStatus, string> = {
    [DefenseStatus.PLANNED]: 'Planifiée',
    [DefenseStatus.ONGOING]: 'En cours',
    [DefenseStatus.PUBLISHED]: 'Publiée'
  };

  transform(value: DefenseStatus): string {
    return this.labels[value] || value;
  }
}
