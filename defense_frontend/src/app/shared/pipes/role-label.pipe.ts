import { Pipe, PipeTransform } from '@angular/core';
import { Role } from '@core/models';

@Pipe({
  name: 'roleLabel',
  standalone: true
})
export class RoleLabelPipe implements PipeTransform {
  private readonly labels: Record<Role, string> = {
    [Role.ADMIN]: 'Administrateur',
    [Role.PROFESSOR]: 'Professeur',
    [Role.STUDENT]: 'Étudiant'
  };

  transform(value: Role): string {
    return this.labels[value] || value;
  }
}
