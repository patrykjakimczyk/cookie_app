import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { RecipesService } from '../recipes.service';
import {
  RecipeDetailsDTO,
  RecipeProductDTO,
} from 'src/app/shared/model/types/recipes-types';
import { Unit } from 'src/app/shared/model/enums/unit.enum';
import { UserService } from 'src/app/shared/services/user-service';
import { MatDialog } from '@angular/material/dialog';
import { DeletePopupComponent } from 'src/app/shared/components/delete-popup/delete-popup.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-recipe-details',
  templateUrl: './recipe-details.component.html',
  styleUrls: ['./recipe-details.component.scss'],
})
export class RecipeDetailsComponent implements OnInit {
  protected recipeDetails: RecipeDetailsDTO | null = null;

  constructor(
    private recipesService: RecipesService,
    private userService: UserService,
    private route: ActivatedRoute,
    private router: Router,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const recipeId = this.route.snapshot.params['id'];

    this.recipesService.getRecipeDetails(recipeId).subscribe({
      next: (recipeDetails: RecipeDetailsDTO) => {
        this.recipeDetails = recipeDetails;
      },
      error: (_) => {
        this.router.navigate(['/']);
      },
    });
  }

  printShortUnit(recipeProduct: RecipeProductDTO) {
    if (recipeProduct.unit === Unit.GRAMS) {
      return 'g';
    } else if (recipeProduct.unit === Unit.MILLILITERS) {
      return 'ml';
    } else {
      return recipeProduct.quantity > 1 ? 'pcs' : 'pc';
    }
  }

  canUserModifyRecipe() {
    return (
      this.userService.user.getValue().username ===
      this.recipeDetails?.creatorName
    );
  }

  deleteRecipe() {
    const deleteRecipeDialog = this.dialog.open(DeletePopupComponent, {
      data: {
        header: 'Are you sure you want to delete this recipe?',
        button: 'Delete recipe',
      },
    });

    deleteRecipeDialog.afterClosed().subscribe((deleteRecipe) => {
      if (deleteRecipe) {
        this.recipesService.deleteRecipe(this.recipeDetails!.id).subscribe({
          next: (_) => {
            this.snackBar.open(`Recipe has been deleted`, 'Okay');
            this.router.navigate(['/recipes']);
          },
        });
      }
    });
  }
}
