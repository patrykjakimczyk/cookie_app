import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AddMealRequest } from 'src/app/shared/model/requests/meals-requests';
import { GetUserGroupsResponse } from 'src/app/shared/model/responses/group-response';
import { MealDTO } from 'src/app/shared/model/types/meals.types';

@Injectable({ providedIn: 'root' })
export class MealsService {
  private readonly url = 'http://localhost:8081/';
  private readonly meals_path = 'meals';
  private readonly group_path = 'group';

  constructor(private http: HttpClient) {}

  getUserMeals(dateAfter: string, dateBefore: string) {
    const params = new HttpParams()
      .append('dateAfter', dateAfter.toString())
      .append('dateBefore', dateBefore.toString());

    return this.http.get<MealDTO[]>(this.url + this.meals_path, {
      params: params,
    });
  }

  addMeal(request: AddMealRequest) {
    return this.http.post<MealDTO>(`${this.url}${this.meals_path}`, request);
  }

  removeMeal(mealId: number) {
    return this.http.delete<void>(`${this.url}${this.meals_path}/${mealId}`);
  }

  getUserGroups() {
    return this.http.get<GetUserGroupsResponse>(this.url + this.group_path);
  }
}
