import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  GetRecipesParams,
  RecipeDTO,
} from 'src/app/shared/model/types/recipes-types';

@Injectable({ providedIn: 'root' })
export class RecipesService {
  private readonly url = 'http://localhost:8081/';
  private readonly recipes_page_path = 'recipes/page/{page}';

  constructor(private http: HttpClient) {}

  getRecipes(
    page: number,
    filterValues: GetRecipesParams
  ): Observable<RecipeDTO[]> {
    let params = new HttpParams();

    params = params
      .append('filterValue', filterValues.filterValue)
      .append('prepTime', filterValues.prepTime)
      .append('portions', filterValues.portions)
      .append('sortColName', filterValues.sortColName)
      .append('sortDirection', filterValues.sortDirection);
    console.log(page);

    return this.http.get<RecipeDTO[]>(
      this.url + this.recipes_page_path.replace('{page}', page.toString()),
      { params: params }
    );
  }
}
