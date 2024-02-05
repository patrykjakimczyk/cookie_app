import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'minutesToHours',
})
export class MinutesToHoursPipe implements PipeTransform {
  transform(value: number | undefined): number | string {
    if (!value) {
      return '';
    }

    return value / 60 > 1
      ? `${this.minutesToHours(value)} ${value / 60 > 2 ? 'hours' : 'hour'}`
      : `${value} minutes`;
  }

  private minutesToHours(value: number) {
    return value % 60 === 0 ? value / 60 : (value / 60).toFixed(1);
  }
}
