import { PageResult } from './../../shared/model/responses/page-result-response';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateRecipeResponse } from 'src/app/shared/model/responses/recipes-response';
import { ProductDTO } from 'src/app/shared/model/types/product-types';
import {
  GetRecipesParams,
  RecipeDTO,
  RecipeDetailsDTO,
} from 'src/app/shared/model/types/recipes-types';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class RecipesService {
  private readonly url = environment.backendUrl;
  private readonly recipes_path = 'recipes';
  private readonly recipes_page_path = 'recipes/page/{page}';
  private readonly user_recipes_page_path = 'recipes/user-recipes/{page}';
  private readonly recipes_details_path = 'recipes/{id}';
  private readonly products_path = 'products';

  constructor(private http: HttpClient) {}

  getAllRecipes(
    page: number,
    filterValues: GetRecipesParams
  ): Observable<PageResult<RecipeDTO>> {
    return this.getRecipes(
      filterValues,
      this.url + this.recipes_page_path.replace('{page}', page.toString())
    );
  }

  getUserRecipes(
    page: number,
    filterValues: GetRecipesParams
  ): Observable<PageResult<RecipeDTO>> {
    return this.getRecipes(
      filterValues,
      this.url + this.user_recipes_page_path.replace('{page}', page.toString())
    );
  }

  private getRecipes(
    filterValues: GetRecipesParams,
    path: string
  ): Observable<PageResult<RecipeDTO>> {
    let params = new HttpParams();

    if (filterValues.filterValue) {
      params = params.append('filterValue', filterValues.filterValue);
    }
    if (filterValues.prepTime) {
      params = params.append('prepTime', filterValues.prepTime);
    }
    if (filterValues.portions) {
      params = params.append('portions', filterValues.portions);
    }
    if (filterValues.sortColName) {
      params = params.append('sortColName', filterValues.sortColName);
    }
    if (filterValues.sortDirection) {
      params = params.append('sortDirection', filterValues.sortDirection);
    }
    if (filterValues.mealTypes.length) {
      for (let mealType of filterValues.mealTypes) {
        params = params.append('mealTypes', mealType);
      }
    }

    return this.http.get<PageResult<RecipeDTO>>(path, { params: params });
  }

  getRecipeDetails(recipeId: number) {
    return this.http.get<RecipeDetailsDTO>(
      this.url + this.recipes_details_path.replace('{id}', recipeId.toString())
    );
  }

  createRecipe(formData: FormData) {
    return this.http.post<CreateRecipeResponse>(
      this.url + this.recipes_path,
      formData
    );
  }

  deleteRecipe(recipeId: number) {
    return this.http.delete<void>(
      this.url + this.recipes_details_path.replace('{id}', recipeId.toString())
    );
  }

  editRecipe(formData: FormData) {
    return this.http.patch<CreateRecipeResponse>(
      this.url + this.recipes_path,
      formData
    );
  }

  getProductsWithFilter(filterValue: string): Observable<ProductDTO[]> {
    let params = new HttpParams();

    params = params.append('filterValue', filterValue);

    return this.http.get<ProductDTO[]>(`${this.url}${this.products_path}`, {
      params: params,
    });
  }
}
