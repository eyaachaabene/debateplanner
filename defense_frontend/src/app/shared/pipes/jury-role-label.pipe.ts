import { Pipe, PipeTransform } from '@angular/core';
import { JuryRole } from '@core/models';

@Pipe({
  name: 'juryRoleLabel',
  standalone: true
})
export class JuryRoleLabelPipe implements PipeTransform {
  private readonly labels: Record<JuryRole, string> = {
    [JuryRole.PRESIDENT]: 'Président',
    [JuryRole.RAPPORTEUR]: 'Rapporteur',
    [JuryRole.EXAMINATEUR]: 'Examinateur',
    [JuryRole.ENCADRANT]: 'Encadrant'
  };

  transform(value: JuryRole): string {
    return this.labels[value] || value;
  }
}
