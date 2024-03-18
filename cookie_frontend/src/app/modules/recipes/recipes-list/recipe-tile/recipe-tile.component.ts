import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RecipeDTO } from 'src/app/shared/model/types/recipes-types';
import { MealPlanningService } from 'src/app/shared/services/meal-planning-service';
import { UserService } from 'src/app/shared/services/user-service';

@Component({
  selector: 'app-recipe-tile',
  templateUrl: './recipe-tile.component.html',
  styleUrls: ['./recipe-tile.component.scss'],
})
export class RecipeTileComponent implements OnInit {
  @Input() recipe!: RecipeDTO;
  @Input() mealPlanning!: boolean;
  protected recipeImage: string = '';

  constructor(
    private router: Router,
    private mealPlanningService: MealPlanningService,
    protected userService: UserService
  ) {}

  ngOnInit(): void {
    if (this.recipe.recipeImage) {
      this.recipeImage =
        'data:image/JPEG;png;base64,' + this.recipe.recipeImage;
    }
  }

  goToRecipeDetails() {
    if (this.mealPlanning) {
      this.router.navigate(['/recipes/' + this.recipe.id], {
        queryParams: { mealPlanning: true },
      });
    } else {
      this.router.navigate(['/recipes/' + this.recipe.id]);
    }
  }

  scheduleMeal() {
    if (this.mealPlanningService.currentMealPlanning) {
      this.mealPlanningService.currentMealPlanning.recipe = this.recipe;
      this.router.navigate(['/meals'], {
        queryParams: { scheduleMeal: true },
      });
    } else {
      this.mealPlanningService.currentMealPlanning = {
        mealDate: null,
        groupId: null,
        recipe: this.recipe,
      };
      this.router.navigate(['/meals'], {
        queryParams: { scheduleMeal: true },
      });
    }
  }
}
