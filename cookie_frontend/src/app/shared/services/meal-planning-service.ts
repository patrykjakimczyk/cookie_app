import { Injectable } from '@angular/core';
import { MealType } from '../model/enums/meal-type.enum';
import { BehaviorSubject } from 'rxjs';

export type RecipeToSchedule = {
  id: number;
  recipeName: string;
  mealType: MealType;
};

export interface MealPlanning {
  mealDate: Date | null;
  groupId: number | null;
  recipe: RecipeToSchedule | null;
}

export interface ModifyMeal extends MealPlanning {
  id: number;
}

@Injectable({ providedIn: 'root' })
export class MealPlanningService {
  private _currentMealPlanning: MealPlanning | null = null;
  private _modifyingMeal$ = new BehaviorSubject<ModifyMeal | null>(null);

  public get currentMealPlanning() {
    return this._currentMealPlanning;
  }

  public set currentMealPlanning(currentMealPlanning: MealPlanning | null) {
    this._currentMealPlanning = currentMealPlanning;
  }

  public get modifyingMeal$() {
    return this._modifyingMeal$;
  }
}
