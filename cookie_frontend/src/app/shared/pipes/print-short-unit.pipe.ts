import { Pipe, PipeTransform } from '@angular/core';
import { Unit } from '../model/enums/unit.enum';

@Pipe({
  standalone: true,
  name: 'printShortUnit',
})
export class PrintShortUnitPipe implements PipeTransform {
  transform(value: string): string {
    if (value === Unit.GRAMS) {
      return 'g';
    } else if (value === Unit.MILLILITERS) {
      return 'ml';
    } else {
      return 'pcs';
    }
  }
}
