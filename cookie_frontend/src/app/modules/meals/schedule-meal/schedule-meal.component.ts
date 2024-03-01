import { GroupDTO } from 'src/app/shared/model/types/group-types';
import { GetUserGroupsResponse } from './../../../shared/model/responses/group-response';
import { MealsService } from './../meals.service';
import { Component, OnInit } from '@angular/core';
import {
  AbstractControl,
  FormBuilder,
  FormGroupDirective,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { RecipeDTO } from 'src/app/shared/model/types/recipes-types';
import {
  CurrentMealPlanningService,
  MealPlanning,
} from 'src/app/shared/services/meal-planning-service';
import { ActivatedRoute, Router } from '@angular/router';
import { EnumPrintFormatterPipe } from 'src/app/shared/pipes/enum-print-formatter.pipe';
import { Observable, of } from 'rxjs';
import { AddMealRequest } from 'src/app/shared/model/requests/meals-requests';

@Component({
  selector: 'app-schedule-meal',
  templateUrl: './schedule-meal.component.html',
  styleUrls: ['./schedule-meal.component.scss'],
})
export class ScheduleMealComponent implements OnInit {
  readonly now = new Date();
  protected userGroups?: GroupDTO[];
  protected chosenDate: Date | null = null;
  protected chosenRecipe: RecipeDTO | null = null;

  protected mealForm = this.fb.group({
    mealDate: ['', Validators.required, this.invalidDateValidation],
    groupId: ['', Validators.required],
  });

  constructor(
    private mealsService: MealsService,
    private fb: FormBuilder,
    private currentMealPlanning: CurrentMealPlanningService,
    private router: Router,
    private route: ActivatedRoute,
    private enumPrintFormatter: EnumPrintFormatterPipe
  ) {}

  ngOnInit(): void {
    this.getUserGroups();

    this.route.queryParams.subscribe((params) => {
      let scheduleMeal = params['scheduleMeal'];

      if (scheduleMeal) {
        // this.mealForm.controls.groupId =
        //   this.currentMealPlanning.currentMealPlanning?.groupId;
        if (this.currentMealPlanning.currentMealPlanning?.mealDate) {
          this.chosenDate =
            this.currentMealPlanning.currentMealPlanning?.mealDate;
        }

        this.chosenRecipe =
          this.currentMealPlanning.currentMealPlanning!.recipe;
      } else {
        this.currentMealPlanning.currentMealPlanning = null;
      }
    });
  }

  getErrorMessage(control: AbstractControl): string {
    if (control.hasError('matDatepickerParse')) {
      return 'Date is incorrect';
    } else if (control.hasError('required')) {
      return 'Field is required';
    } else if (control.hasError('invalidHour')) {
      return "Meal's hour must be beetween 6 and 22 o'clock";
    } else if (control.hasError('matDatetimePickerMin')) {
      return 'Date must be set in future';
    }

    return '';
  }

  submitMealForm(form: FormGroupDirective) {
    console.log(this.mealForm.value);
    if (!this.mealForm.valid && !this.chosenRecipe) {
      return;
    }

    const request: AddMealRequest = {
      mealDate: new Date(this.mealForm.controls.mealDate.value!),
      groupId: +this.mealForm.controls.groupId.value!,
      recipeId: this.chosenRecipe!.id!,
    };

    this.mealsService.addMeal(request).subscribe({
      next: (response) => {},
    });
    form.resetForm(); // this combination of two resets allows to reset form without displaying form fields as invalid
    this.mealForm.reset();
  }

  goToRecipes() {
    const mealDate =
      this.mealForm.controls.mealDate.valid &&
      this.mealForm.controls.mealDate.value
        ? new Date(this.mealForm.controls.mealDate.value)
        : null;

    const mealPlanning: MealPlanning = {
      mealDate: mealDate,
      groupId: this.mealForm.controls.groupId.value
        ? +this.mealForm.controls.groupId.value
        : 0,
      recipe: this.chosenRecipe,
    };

    this.currentMealPlanning.currentMealPlanning = mealPlanning;
    this.router.navigate(['/recipes']);
  }

  printRecipe() {
    return this.chosenRecipe
      ? `${
          this.chosenRecipe.recipeName
        }, meal type: ${this.enumPrintFormatter.transform(
          this.chosenRecipe.mealType
        )}`
      : '';
  }

  private invalidDateValidation(
    control: AbstractControl
  ): Observable<ValidationErrors | null> {
    if (control.value) {
      const hour = new Date(control.value).getHours();

      return 6 < hour && hour < 23 ? of(null) : of({ invalidHour: true });
    }
    return of(null);
  }

  private getUserGroups() {
    this.mealsService.getUserGroups().subscribe({
      next: (response: GetUserGroupsResponse) => {
        this.userGroups = response.userGroups;
      },
    });
  }
}
