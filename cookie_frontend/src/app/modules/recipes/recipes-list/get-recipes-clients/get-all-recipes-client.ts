import { Injectable, Injector } from '@angular/core';
import { GetRecipesClient } from './abstract-get-recipes-client';
import { GetRecipesParams } from 'src/app/shared/model/types/recipes-types';

@Injectable({ providedIn: 'root' })
export class GetAllRecipesClient extends GetRecipesClient {
  constructor(private injector: Injector) {
    super(injector);
  }

  override getRecipes(params: GetRecipesParams): void {
    this.recipesService
      .getAllRecipes(this.page, params)
      .subscribe((recipes: any) => {
        this.saveResponseData(recipes);
      });
  }
}
