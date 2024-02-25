import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { S } from '@fullcalendar/core/internal-common';
import { MealDTO } from 'src/app/shared/model/types/meals.types';

@Injectable({ providedIn: 'root' })
export class MealsService {
  private readonly url = 'http://localhost:8081/';
  private readonly meals_path = 'meals';

  constructor(private http: HttpClient) {}

  getUserMeals(dateAfter: string, dateBefore: string) {
    const params = new HttpParams()
      .append('dateAfter', dateAfter.toString())
      .append('dateBefore', dateBefore.toString());

    return this.http.get<MealDTO[]>(this.url + this.meals_path, {
      params: params,
    });
  }
}
