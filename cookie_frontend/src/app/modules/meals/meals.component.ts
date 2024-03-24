import { EnumPrintFormatterPipe } from './../../shared/pipes/enum-print-formatter.pipe';
import { AfterViewInit, Component, OnInit, ViewChild } from '@angular/core';
import { calendarConfig } from './calendar.config';
import { MealsService } from './meals.service';
import { FullCalendarComponent } from '@fullcalendar/angular';
import { MealDTO } from 'src/app/shared/model/types/meals.types';
import { MatDialog } from '@angular/material/dialog';
import { MealDetailsPopupComponent } from './meal-details-popup/meal-details-popup.component';
import { MealType } from 'src/app/shared/model/enums/meal-type.enum';

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
    private enumPrintFormatter: EnumPrintFormatterPipe,
    private dialog: MatDialog
  ) {}

  ngAfterViewInit(): void {
    this.getMeals();

    this.calendar.options = {
      datesSet: () => this.getMeals(),
      eventClick: (event) =>
        this.openMealDetailsPopup(event.event.extendedProps['meal']),
    };
  }

  addMeal(meal: MealDTO) {
    this.calendar.getApi().addEvent(this.mapToEventObject(meal));
    this.calendar.getApi().render();
  }

  modifyMeal(meal: MealDTO) {
    const eventToRemove = this.calendar
      .getApi()
      .getEventById(meal.id.toString());

    if (eventToRemove) {
      eventToRemove.remove();
    }
    this.calendar.getApi().addEvent(this.mapToEventObject(meal));
    this.calendar.getApi().render();
  }

  private openMealDetailsPopup(meal: MealDTO) {
    const mealDetailsDialog = this.dialog.open(MealDetailsPopupComponent, {
      data: meal,
    });

    mealDetailsDialog.afterClosed().subscribe((result) => {
      if (result) {
        const eventToRemove = this.calendar.getApi().getEventById(result);

        if (eventToRemove) {
          eventToRemove.remove();
          this.calendar.getApi().render();
        }
      }
    });
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
    const title = `${this.enumPrintFormatter.transform(
      meal.recipe.mealType
    )}: ${meal.recipe.recipeName}, for group: ${meal.group.groupName}`;

    return {
      id: String(meal.id),
      title: title,
      description: title,
      start: meal.mealDate,
      meal: meal,
      color: this.getEventColor(meal.recipe.mealType),
    };
  }

  private getEventColor(mealType: MealType): string {
    switch (mealType) {
      case MealType.APPETIZER:
        return '#9900ff';
      case MealType.BREAKFAST:
        return '#33cc33';
      case MealType.DESSERT:
        return '#ff33cc';
      case MealType.DINNER:
        return '#ff9900';
      case MealType.LUNCH:
        return '#669999';
      case MealType.SNACK:
        return '#ff3300';
      case MealType.SOUP:
        return '#3399ff';
      default:
        return '#004080';
    }
  }
}
