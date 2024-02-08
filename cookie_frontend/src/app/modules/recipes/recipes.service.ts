import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  GetRecipesParams,
  RecipeDTO,
  RecipeDetailsDTO,
} from 'src/app/shared/model/types/recipes-types';

@Injectable({ providedIn: 'root' })
export class RecipesService {
  private readonly url = 'http://localhost:8081/';
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

    return this.http.get<RecipeDTO[]>(path, { params: params });
  }

  getRecipeDetails(recipeId: number) {
    return this.http.get<RecipeDetailsDTO>(
      this.url + this.recipes_details_path.replace('{id}', recipeId.toString())
    );
  }

  createRecipe(recipe: RecipeDetailsDTO) {
    console.log(recipe);
    return this.http.post<RecipeDetailsDTO>(
      this.url + this.recipes_path,
      recipe
    );
  }

  deleteRecipe(recipeId: number) {
    return this.http.delete<void>(
      this.url + this.recipes_details_path.replace('{id}', recipeId.toString())
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
