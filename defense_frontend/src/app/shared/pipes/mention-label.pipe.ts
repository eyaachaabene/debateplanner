import { Pipe, PipeTransform } from '@angular/core';
import { Mention } from '@core/models';

@Pipe({
  name: 'mentionLabel',
  standalone: true
})
export class MentionLabelPipe implements PipeTransform {
  private readonly labels: Record<Mention, string> = {
    [Mention.FAIL]: 'Ajourné',
    [Mention.FAIRLY_GOOD]: 'Assez bien',
    [Mention.PASSABLE]: 'Passable',
    [Mention.GOOD]: 'Bien',
    [Mention.VERY_GOOD]: 'Très bien',
    [Mention.EXCELLENT]: 'Excellent'
  };

  transform(value: Mention | string): string {
    return this.labels[value as Mention] || value;
  }
}
