import { Injectable, Injector } from '@angular/core';
import { GetRecipesClient } from './abstract-get-recipes-client';
import {
  GetRecipesParams,
  RecipeDTO,
} from 'src/app/shared/model/types/recipes-types';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class GetAllRecipesClient extends GetRecipesClient {
  constructor(private injector: Injector) {
    super(injector);
  }

  override getRecipes(params: GetRecipesParams): void {
    this.recipesService.getRecipes(this.page, params).subscribe({
      next: (recipes: any) => {
        this.saveResponseData(recipes);
      },
    });
  }
}
