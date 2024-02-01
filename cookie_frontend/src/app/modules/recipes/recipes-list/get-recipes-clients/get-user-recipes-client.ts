import { Injectable, Injector } from '@angular/core';
import { GetRecipesClient } from './abstract-get-recipes-client';
import { GetRecipesParams } from 'src/app/shared/model/types/recipes-types';

@Injectable({ providedIn: 'root' })
export class GetUserRecipesClient extends GetRecipesClient {
  constructor(private injector: Injector) {
    super(injector);
  }

  override getRecipes(params: GetRecipesParams): void {
    this.recipesService.getUserRecipes(this.page, params).subscribe({
      next: (recipes: any) => {
        this.saveResponseData(recipes);
      },
    });
  }
}
