import { Pipe, PipeTransform } from '@angular/core';
import { Major } from '@core/models';

@Pipe({
  name: 'majorLabel',
  standalone: true
})
export class MajorLabelPipe implements PipeTransform {
  private readonly labels: Record<Major, string> = {
    [Major.GINFO]: 'Génie Informatique',
    [Major.GELE]: 'Génie Électrique',
    [Major.GMEC]: 'Génie Mécanique',
    [Major.GCIV]: 'Génie Civil'
  };

  transform(value: Major): string {
    return this.labels[value] || value;
  }
}
