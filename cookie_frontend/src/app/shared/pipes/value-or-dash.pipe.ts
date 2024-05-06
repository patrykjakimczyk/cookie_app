import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  standalone: true,
  name: 'valueOrDash',
})
export class ValueOrDashPipe implements PipeTransform {
  transform(value: string | undefined | null): string {
    return value ? value : '-';
  }
}
