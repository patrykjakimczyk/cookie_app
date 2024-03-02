import { GroupDTO } from 'src/app/shared/model/types/group-types';
import { GetUserGroupsResponse } from './../../../shared/model/responses/group-response';
import { MealsService } from './../meals.service';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
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
  MealToSchedule,
} from 'src/app/shared/services/meal-planning-service';
import { ActivatedRoute, Router } from '@angular/router';
import { EnumPrintFormatterPipe } from 'src/app/shared/pipes/enum-print-formatter.pipe';
import { Observable, of } from 'rxjs';
import { AddMealRequest } from 'src/app/shared/model/requests/meals-requests';
import { MealDTO } from 'src/app/shared/model/types/meals.types';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-schedule-meal',
  templateUrl: './schedule-meal.component.html',
  styleUrls: ['./schedule-meal.component.scss'],
})
export class ScheduleMealComponent implements OnInit {
  @Output() addedMeal = new EventEmitter<MealDTO>();
  readonly now = new Date();
  protected userGroups?: GroupDTO[];
  protected chosenDate: Date | null = null;
  protected chosenGroupId: number | null = null;
  protected chosenRecipe: MealToSchedule | null = null;
  protected showUnselectedRecipe = false;

  protected mealForm = this.fb.group({
    mealDate: ['', Validators.required, this.invalidDateValidation],
    groupId: ['', Validators.required],
    recipe: ['', Validators.required],
  });

  constructor(
    private mealsService: MealsService,
    private fb: FormBuilder,
    private currentMealPlanning: CurrentMealPlanningService,
    private router: Router,
    private route: ActivatedRoute,
    private enumPrintFormatter: EnumPrintFormatterPipe,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.getUserGroups();

    this.route.queryParams.subscribe((params) => {
      let scheduleMeal = params['scheduleMeal'];

      if (scheduleMeal) {
        if (this.currentMealPlanning.currentMealPlanning?.mealDate) {
          this.chosenDate =
            this.currentMealPlanning.currentMealPlanning?.mealDate;
        }

        if (this.currentMealPlanning.currentMealPlanning?.groupId) {
          this.chosenGroupId =
            this.currentMealPlanning.currentMealPlanning?.groupId;
        }

        this.chosenRecipe =
          this.currentMealPlanning.currentMealPlanning!.recipe;

        this.mealForm.controls.recipe.setValue(this.printRecipe());
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
    if (!this.mealForm.valid) {
      if (!this.chosenRecipe) {
        this.showUnselectedRecipe = true;
      }
      return;
    }
    this.showUnselectedRecipe = false;

    const request: AddMealRequest = {
      mealDate: new Date(this.mealForm.controls.mealDate.value!),
      groupId: +this.mealForm.controls.groupId.value!,
      recipeId: this.chosenRecipe!.id!,
    };

    this.mealsService.addMeal(request).subscribe({
      next: (response: MealDTO) => {
        this.addedMeal.emit(response);
        this.snackBar.open(`Meal has been removed from calendar`, 'Okay');

        this.chosenDate = null;
        this.chosenGroupId = null;
        this.chosenRecipe = null;
        this.currentMealPlanning.currentMealPlanning = null;
      },
    });

    form.resetForm(); // this combination of two resets allows to reset form without displaying form fields as invalid
    this.mealForm.reset();
  }

  goToRecipes() {
    const mealPlanning = this.createMealPlanningObj();
    this.currentMealPlanning.currentMealPlanning = mealPlanning;

    this.router.navigate(['/recipes'], {
      queryParams: { mealPlanning: 'true' },
    });
  }

  goToRecipeDetails() {
    const mealPlanning = this.createMealPlanningObj();
    this.currentMealPlanning.currentMealPlanning = mealPlanning;

    this.router.navigate(['/recipes', this.chosenRecipe!.id], {
      queryParams: { mealPlanning: 'true' },
    });
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

  private createMealPlanningObj(): MealPlanning {
    const mealDate =
      this.mealForm.controls.mealDate.valid &&
      this.mealForm.controls.mealDate.value
        ? new Date(this.mealForm.controls.mealDate.value)
        : null;

    return {
      mealDate: mealDate,
      groupId: this.mealForm.controls.groupId.value
        ? +this.mealForm.controls.groupId.value
        : 0,
      recipe: this.chosenRecipe,
    };
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
