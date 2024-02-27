import { EnumPrintFormatterPipe } from './../../shared/pipes/enum-print-formatter.pipe';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { calendarConfig } from './calendar.config';
import { MealsService } from './meals.service';
import { FullCalendarComponent } from '@fullcalendar/angular';
import { MealDTO } from 'src/app/shared/model/types/meals.types';

@Component({
  selector: 'app-meals',
  templateUrl: './meals.component.html',
  styleUrls: ['./meals.component.scss'],
})
export class MealsComponent implements AfterViewInit {
  @ViewChild('calendar') calendar!: FullCalendarComponent;
  calendarOptions = calendarConfig;

  constructor(
    private mealsService: MealsService,
    private enumPrintFormatter: EnumPrintFormatterPipe
  ) {}

  ngAfterViewInit(): void {
    this.getMeals();

    this.calendar.options = {
      datesSet: () => this.getMeals(),
    };
  }

  private getMeals() {
    const dateAfter = this.formatISOString(
      this.calendar.getApi().view.activeStart.toISOString()
    );
    const dateBefore = this.formatISOString(
      this.calendar.getApi().view.activeEnd.toISOString()
    );

    this.mealsService.getUserMeals(dateAfter, dateBefore).subscribe({
      next: (meals) => {
        this.calendar.events = meals.map((meal) => this.mapToEventObject(meal));
      },
    });
  }

  private formatISOString(isoString: string) {
    return isoString.replace('T', ' ').replace('Z', '');
  }

  private mapToEventObject(meal: MealDTO) {
    return {
      title: `${this.enumPrintFormatter.transform(meal.recipe.mealType)}: ${
        meal.recipe.recipeName
      }`,
      description: `${this.enumPrintFormatter.transform(
        meal.recipe.mealType
      )}: ${meal.recipe.recipeName}`,
      start: meal.mealDate,
    };
  }
}
