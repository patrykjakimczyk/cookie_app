import { Component, Input } from '@angular/core';
import { RecipeDTO } from 'src/app/shared/model/types/recipes-types';

@Component({
  selector: 'app-recipe-tile',
  templateUrl: './recipe-tile.component.html',
  styleUrls: ['./recipe-tile.component.scss'],
})
export class RecipeTileComponent {
  @Input() recipe!: RecipeDTO;
}
