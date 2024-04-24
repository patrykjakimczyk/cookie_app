import { Injectable, Injector } from '@angular/core';
import { RecipesService } from '../../recipes.service';
import {
  GetRecipesParams,
  RecipeDTO,
} from 'src/app/shared/model/types/recipes-types';
import { PageResult } from 'src/app/shared/model/responses/page-result-response';

@Injectable({ providedIn: 'root' })
export abstract class GetRecipesClient {
  public readonly page_size = 20;
  protected recipesService: RecipesService;
  page: number;
  totalPages: number;
  totalElements: number;
  recipes: RecipeDTO[];

  constructor(injector: Injector) {
    this.recipesService = injector.get(RecipesService);
    this.page = 0;
    this.totalPages = 0;
    this.totalElements = 0;
    this.recipes = [];
  }

  abstract getRecipes(params: GetRecipesParams): void;

  protected saveResponseData(recipes: PageResult<RecipeDTO>) {
    this.totalPages = recipes.totalPages;
    this.totalElements = recipes.totalElements;
    this.recipes = recipes.content;
  }
}
