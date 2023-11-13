import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'dateFromString',
})
export class DateFromStringPipe implements PipeTransform {
  transform(value: string): string {
    const date = new Date(value);

    return `${date.getDate()}-${date.getMonth() + 1}-${date.getFullYear()}`;
  }
}
