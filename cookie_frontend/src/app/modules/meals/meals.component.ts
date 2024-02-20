import { Component } from '@angular/core';
import { calendarConfig } from './calendar.config';

@Component({
  selector: 'app-meals',
  templateUrl: './meals.component.html',
  styleUrls: ['./meals.component.scss'],
})
export class MealsComponent {
  calendarOptions = calendarConfig;
}
