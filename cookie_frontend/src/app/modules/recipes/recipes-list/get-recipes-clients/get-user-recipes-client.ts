import { Injectable, Injector } from '@angular/core';
import { GetRecipesClient } from './abstract-get-recipes-client';
import {
  GetRecipesParams,
  RecipeDTO,
} from 'src/app/shared/model/types/recipes-types';
import { PageResult } from 'src/app/shared/model/responses/page-result-response';

@Injectable({ providedIn: 'root' })
export class GetUserRecipesClient extends GetRecipesClient {
  constructor(private injector: Injector) {
    super(injector);
  }

  override getRecipes(params: GetRecipesParams): void {
    this.recipesService
      .getUserRecipes(this.page + 1, params)
      .subscribe((recipes: PageResult<RecipeDTO>) => {
        this.saveResponseData(recipes);
      });
  }
}
