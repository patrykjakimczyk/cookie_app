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
    private snackBar: MatSnackBar
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

  modifyMeal() {}

  close() {
    this.dialogRef.close();
  }
}
