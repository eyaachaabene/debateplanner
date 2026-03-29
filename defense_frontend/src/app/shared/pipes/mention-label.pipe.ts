import { Pipe, PipeTransform } from '@angular/core';
import { Mention } from '@core/models';

@Pipe({
  name: 'mentionLabel',
  standalone: true
})
export class MentionLabelPipe implements PipeTransform {
  private readonly labels: Record<Mention, string> = {
    [Mention.TRES_HONORABLE]: 'Très Honorable',
    [Mention.HONORABLE]: 'Honorable',
    [Mention.FAIRLY_GOOD]: 'Assez bien',
    [Mention.PASSABLE]: 'Passable',
    [Mention.AJOURNE]: 'Ajourné'
  };

  transform(value: Mention | string): string {
    return this.labels[value as Mention] || value;
  }
}
