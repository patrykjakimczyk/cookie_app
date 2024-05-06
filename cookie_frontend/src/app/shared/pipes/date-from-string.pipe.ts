import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'dateFromString',
})
export class DateFromStringPipe implements PipeTransform {
  transform(value: string | null | undefined): string {
    if (!value) {
      return '';
    }

    const date = new Date(
      +value.substring(6, 10),
      +value.substring(3, 5) - 1,
      +value.substring(0, 2)
    );

    return `${date.getDate()}-${date.getMonth() + 1}-${date.getFullYear()}`;
  }
}
