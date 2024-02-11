import { Component, Input, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { RecipeDTO } from 'src/app/shared/model/types/recipes-types';

@Component({
  selector: 'app-recipe-tile',
  templateUrl: './recipe-tile.component.html',
  styleUrls: ['./recipe-tile.component.scss'],
})
export class RecipeTileComponent implements OnInit {
  @Input() recipe!: RecipeDTO;
  protected recipeImage: string = '';

  constructor(private router: Router) {}

  ngOnInit(): void {
    if (this.recipe.recipeImage) {
      this.recipeImage =
        'data:image/JPEG;png;base64,' + this.recipe.recipeImage;
    }
  }

  goToRecipeDetails() {
    this.router.navigate(['/recipes/' + this.recipe.id]);
  }
}
