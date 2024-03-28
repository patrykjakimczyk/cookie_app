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
import {
  MealPlanningService,
  MealPlanning,
  RecipeToSchedule,
} from 'src/app/shared/services/meal-planning-service';
import { ActivatedRoute, Router } from '@angular/router';
import { EnumPrintFormatterPipe } from 'src/app/shared/pipes/enum-print-formatter.pipe';
import { Observable, firstValueFrom, of } from 'rxjs';
import { AddMealRequest } from 'src/app/shared/model/requests/meals-requests';
import { MealDTO } from 'src/app/shared/model/types/meals.types';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatDialog } from '@angular/material/dialog';
import { ReserveProductsPopupComponent } from '../reserve-products-popup/reserve-products-popup.component';
import { AddToListPopupComponent } from '../add-to-list-popup/add-to-list-popup.component';

@Component({
  selector: 'app-schedule-meal',
  templateUrl: './schedule-meal.component.html',
  styleUrls: ['./schedule-meal.component.scss'],
})
export class ScheduleMealComponent implements OnInit {
  @Output() addedMeal = new EventEmitter<MealDTO>();
  @Output() modifiedMeal = new EventEmitter<MealDTO>();
  readonly now = new Date();
  protected userGroups?: GroupDTO[];
  protected chosenDate: Date | null = null;
  protected chosenGroupId: number | null = null;
  protected chosenRecipe: RecipeToSchedule | null = null;
  protected showUnselectedRecipe = false;
  protected modifyingMeal = false;
  protected mealToModifyId = 0;

  protected mealForm = this.fb.group({
    mealDate: ['', Validators.required, this.invalidDateValidation],
    groupId: ['', Validators.required],
    recipe: ['', Validators.required],
  });

  constructor(
    private mealsService: MealsService,
    private fb: FormBuilder,
    private mealPlanningService: MealPlanningService,
    private router: Router,
    private route: ActivatedRoute,
    private enumPrintFormatter: EnumPrintFormatterPipe,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  ngOnInit(): void {
    this.getUserGroups();
    this.setModifyMealSubscription();

    this.route.queryParams.subscribe((params) => {
      let scheduleMeal = params['scheduleMeal'];

      if (scheduleMeal) {
        if (this.mealPlanningService.currentMealPlanning?.mealDate) {
          this.chosenDate =
            this.mealPlanningService.currentMealPlanning?.mealDate;
        }

        if (this.mealPlanningService.currentMealPlanning?.groupId) {
          this.chosenGroupId =
            this.mealPlanningService.currentMealPlanning?.groupId;
        }

        this.chosenRecipe =
          this.mealPlanningService.currentMealPlanning!.recipe;

        this.mealForm.controls.recipe.setValue(this.printRecipe());
      } else {
        this.mealPlanningService.currentMealPlanning = null;
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

  async submitMealForm(form: FormGroupDirective) {
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

    if (this.modifyingMeal) {
      this.mealsService
        .modifyMeal(this.mealToModifyId, request)
        .subscribe((response: MealDTO) => {
          this.modifiedMeal.emit(response);
          this.snackBar.open(`Meal has been modified`, 'Okay');

          this.clear();
          form.resetForm(); // this combination of two resets allows to reset form without displaying form fields as invalid
          this.mealForm.reset();
        });
    } else {
      const chosenGroup = this.userGroups?.find(
        (group) => group.id === this.chosenGroupId
      );

      let reserve = false;
      let addToList: number | null = null;

      if (chosenGroup?.pantryId) {
        const reserveProductsDialog = this.dialog.open(
          ReserveProductsPopupComponent
        );

        reserve = await firstValueFrom(reserveProductsDialog.afterClosed());
      }

      const groupDetails = await firstValueFrom(
        this.mealsService.getGroupDetails(this.chosenGroupId!)
      );

      if (groupDetails.shoppingLists.length > 0) {
        const addToListDialog = this.dialog.open(AddToListPopupComponent, {
          data: groupDetails.shoppingLists,
        });

        addToList = await firstValueFrom(addToListDialog.afterClosed());
      }

      this.addMealSubscription(form, request, reserve, addToList);
    }
  }

  goToRecipes() {
    const mealPlanning = this.createMealPlanningObj();
    this.mealPlanningService.currentMealPlanning = mealPlanning;

    this.router.navigate(['/recipes'], {
      queryParams: { mealPlanning: 'true' },
    });
  }

  goToRecipeDetails() {
    const mealPlanning = this.createMealPlanningObj();
    this.mealPlanningService.currentMealPlanning = mealPlanning;

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

  clear() {
    this.modifyingMeal = false;
    this.mealToModifyId = 0;
    this.chosenDate = null;
    this.chosenGroupId = null;
    this.chosenRecipe = null;
    this.mealPlanningService.currentMealPlanning = null;
    this.mealPlanningService.modifyingMeal$.next(null);
  }

  private addMealSubscription(
    form: FormGroupDirective,
    request: AddMealRequest,
    reserve: boolean,
    listId: number | null
  ) {
    this.mealsService
      .addMeal(request, reserve, listId)
      .subscribe((response: MealDTO) => {
        this.addedMeal.emit(response);
        this.snackBar.open(`Meal has been added to calendar`, 'Okay');

        this.clear();
        form.resetForm(); // this combination of two resets allows to reset form without displaying form fields as invalid
        this.mealForm.reset();
      });
  }

  private setModifyMealSubscription() {
    this.mealPlanningService.modifyingMeal$.subscribe((mealToModify) => {
      if (mealToModify) {
        this.modifyingMeal = true;
        this.mealToModifyId = mealToModify.id;
        this.chosenDate = mealToModify.mealDate;
        this.chosenGroupId = mealToModify.groupId;
        this.chosenRecipe = mealToModify.recipe;
        this.mealForm.controls.recipe.setValue(this.printRecipe());
      } else {
        this.modifyingMeal = false;
      }
    });
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
    this.mealsService
      .getUserGroups()
      .subscribe((response: GetUserGroupsResponse) => {
        this.userGroups = response.userGroups;
      });
  }
}
