import { Injectable } from '@angular/core';
import { RecipeDTO } from '../model/types/recipes-types';

export type MealPlanning = {
  mealDate: Date | null;
  groupId: number | null;
  recipe: RecipeDTO | null;
};

@Injectable({ providedIn: 'root' })
export class CurrentMealPlanningService {
  private _currentMealPlanning: MealPlanning | null = null;

  public get currentMealPlanning() {
    return this._currentMealPlanning;
  }

  public set currentMealPlanning(currentMealPlanning: MealPlanning | null) {
    this._currentMealPlanning = currentMealPlanning;
  }
}
