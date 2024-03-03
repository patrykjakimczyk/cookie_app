import {
  MealPlanning,
  MealPlanningService,
  ModifyMeal,
  RecipeToSchedule,
} from './../../../shared/services/meal-planning-service';
import { Component, Inject } from '@angular/core';
import {
  MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef,
} from '@angular/material/dialog';
import { Router } from '@angular/router';
import { DeletePopupComponent } from 'src/app/shared/components/delete-popup/delete-popup.component';
import { MealDTO } from 'src/app/shared/model/types/meals.types';
import { MealsService } from '../meals.service';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-meal-details-popup',
  templateUrl: './meal-details-popup.component.html',
  styleUrls: ['./meal-details-popup.component.scss'],
})
export class MealDetailsPopupComponent {
  constructor(
    public dialogRef: MatDialogRef<MealDetailsPopupComponent>,
    @Inject(MAT_DIALOG_DATA) public meal: MealDTO,
    private dialog: MatDialog,
    private router: Router,
    private mealsService: MealsService,
    private snackBar: MatSnackBar,
    private mealPlanningService: MealPlanningService
  ) {}

  goToRecipe() {
    this.dialogRef.close();
    this.router.navigate(['/recipes/', this.meal.recipe.id]);
  }

  removeMeal() {
    const deleteMealDialog = this.dialog.open(DeletePopupComponent, {
      data: {
        header: 'Are you sure you want to remove this meal from calendar?',
        button: 'Remove recipe',
      },
    });

    deleteMealDialog.afterClosed().subscribe((removeMeal) => {
      if (removeMeal) {
        this.mealsService.removeMeal(this.meal!.id).subscribe({
          next: (_) => {
            this.dialogRef.close(this.meal!.id);
            this.snackBar.open(`Meal has been removed from calendar`, 'Okay');
          },
        });
      }
    });
  }

  modifyMeal() {
    const recipe: RecipeToSchedule = {
      id: this.meal.recipe.id,
      recipeName: this.meal.recipe.recipeName,
      mealType: this.meal.recipe.mealType,
    };
    const modifyMeal: ModifyMeal = {
      id: this.meal.id,
      mealDate: this.meal.mealDate,
      groupId: this.meal.group.id,
      recipe: recipe,
    };

    this.mealPlanningService.modifyingMeal$.next(modifyMeal);
    this.close();
  }

  isMealDateInPast() {
    return new Date(this.meal.mealDate).getTime() <= new Date().getTime();
  }

  close() {
    this.dialogRef.close();
  }
}
