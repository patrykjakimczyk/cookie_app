import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { AddMealRequest } from 'src/app/shared/model/requests/meals-requests';
import { GetUserGroupsResponse } from 'src/app/shared/model/responses/group-response';
import { GroupDetailsDTO } from 'src/app/shared/model/types/group-types';
import { MealDTO } from 'src/app/shared/model/types/meals.types';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class MealsService {
  private readonly url = environment.backendUrl;
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

  addMeal(request: AddMealRequest, reserve: boolean, listId: number | null) {
    let params = new HttpParams();
    params = params.append('reserve', reserve);

    if (listId) {
      params = params.append('listId', listId);
    }

    return this.http.post<MealDTO>(`${this.url}${this.meals_path}`, request, {
      params: params,
    });
  }

  removeMeal(mealId: number) {
    return this.http.delete<void>(`${this.url}${this.meals_path}/${mealId}`);
  }

  modifyMeal(mealId: number, request: AddMealRequest) {
    return this.http.patch<MealDTO>(
      `${this.url}${this.meals_path}/${mealId}`,
      request
    );
  }

  getUserGroups() {
    return this.http.get<GetUserGroupsResponse>(this.url + this.group_path);
  }

  getGroupDetails(id: number) {
    return this.http.get<GroupDetailsDTO>(
      `${this.url}${this.group_path}/${id}`
    );
  }
}
