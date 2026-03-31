import { Pipe, PipeTransform } from '@angular/core';
import { Major } from '@core/models';

@Pipe({
  name: 'majorLabel',
  standalone: true
})
export class MajorLabelPipe implements PipeTransform {
  private readonly labels: Record<Major, string> = {
    [Major.LICENCE_BIG_DATA]: 'Licence Big Data',
    [Major.LICENCE_MULTIMEDIA]: 'Licence Multimédia',
    [Major.MASTER_SOFTWARE_ENGINEERING]: 'Master Génie Logiciel',
    [Major.MASTER_ARTIFICIAL_INTELLIGENCE]: 'Master Intelligence Artificielle',
    [Major.ENGINEERING_COMPUTER_SCIENCE]: 'Ingénierie Informatique',
    [Major.DOCTORATE_COMPUTER_SCIENCE]: 'Doctorat Informatique'
  };

  transform(value: Major): string {
    return this.labels[value] || value;
  }
}
