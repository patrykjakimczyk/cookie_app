import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'enumPrintFormatter',
})
export class EnumPrintFormatterPipe implements PipeTransform {
  transform(value: string): string {
    if (!value) {
      return '';
    }

    return (
      `${value.charAt(0).toUpperCase()}` +
      `${value.slice(1, value.length).toLowerCase()}`.replaceAll('_', ' ')
    );
  }
}
