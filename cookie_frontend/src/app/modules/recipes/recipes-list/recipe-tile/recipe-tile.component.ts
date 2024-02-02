import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';
import { RecipeDTO } from 'src/app/shared/model/types/recipes-types';

@Component({
  selector: 'app-recipe-tile',
  templateUrl: './recipe-tile.component.html',
  styleUrls: ['./recipe-tile.component.scss'],
})
export class RecipeTileComponent {
  @Input() recipe!: RecipeDTO;

  constructor(private router: Router) {}

  goToRecipeDetails() {
    this.router.navigate(['/recipes/' + this.recipe.id]);
  }
}
