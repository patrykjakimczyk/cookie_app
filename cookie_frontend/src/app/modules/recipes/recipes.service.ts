import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { CreateRecipeResponse } from 'src/app/shared/model/responses/recipes-response';
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
  ): Observable<RecipeDTO[]> {
    return this.getRecipes(
      filterValues,
      this.url + this.recipes_page_path.replace('{page}', page.toString())
    );
  }

  getUserRecipes(
    page: number,
    filterValues: GetRecipesParams
  ): Observable<RecipeDTO[]> {
    return this.getRecipes(
      filterValues,
      this.url + this.user_recipes_page_path.replace('{page}', page.toString())
    );
  }

  private getRecipes(
    filterValues: GetRecipesParams,
    path: string
  ): Observable<RecipeDTO[]> {
    let params = new HttpParams();

    params = params
      .append('filterValue', filterValues.filterValue)
      .append('prepTime', filterValues.prepTime)
      .append('portions', filterValues.portions)
      .append('sortColName', filterValues.sortColName)
      .append('sortDirection', filterValues.sortDirection);

    if (filterValues.mealTypes.length === 0) {
      params = params.append('mealTypes', '');
    } else {
      for (let mealType of filterValues.mealTypes) {
        params = params.append('mealTypes', mealType);
      }
    }

    return this.http.get<RecipeDTO[]>(path, { params: params });
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

  getProductsWithFilter(filterValue: string): Observable<any> {
    let params = new HttpParams();

    params = params.append('filterValue', filterValue);

    return this.http.get<any>(`${this.url}${this.products_path}`, {
      params: params,
    });
  }
}
