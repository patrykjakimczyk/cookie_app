import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'categoryNameFormatter',
})
export class CategoryNameFormatterPipe implements PipeTransform {
  transform(value: string): string {
    return (
      `${value.charAt(0).toUpperCase()}` +
      `${value.slice(1, value.length).toLowerCase()}`.replaceAll('_', ' ')
    );
  }
}
