import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'valueOrDash',
})
export class ValueOrDashPipe implements PipeTransform {
  transform(value: string | undefined): string {
    return value ? value : '-';
  }
}
