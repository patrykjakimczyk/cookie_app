import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RecipeDTO } from 'src/app/shared/model/types/recipes-types';
import { CurrentMealPlanningService } from 'src/app/shared/services/meal-planning-service';

@Component({
  selector: 'app-recipe-tile',
  templateUrl: './recipe-tile.component.html',
  styleUrls: ['./recipe-tile.component.scss'],
})
export class RecipeTileComponent implements OnInit {
  @Input() recipe!: RecipeDTO;
  protected recipeImage: string = '';
  private clearMealPlanning = true;

  constructor(
    private router: Router,
    private currentMealPlanning: CurrentMealPlanningService
  ) {}

  ngOnInit(): void {
    if (this.recipe.recipeImage) {
      this.recipeImage =
        'data:image/JPEG;png;base64,' + this.recipe.recipeImage;
    }
  }

  goToRecipeDetails() {
    this.router.navigate(['/recipes/' + this.recipe.id]);
  }

  scheduleMeal() {
    this.clearMealPlanning = false;

    if (this.currentMealPlanning.currentMealPlanning) {
      this.currentMealPlanning.currentMealPlanning.recipe = this.recipe;
      this.router.navigate(['/meals'], {
        queryParams: { scheduleMeal: true },
      });
    } else {
      this.currentMealPlanning.currentMealPlanning = {
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
