import { MealType } from '../enums/meal-type.enum';
import { RecipeProductDTO } from '../types/recipes-types';

export type CreateRecipeRequest = {
  recipeName: string;
  preparation: string;
  preparationTime: number;
  mealType: MealType;
  cuisine: string;
  portions: number;
  updateImage: boolean;
  products: RecipeProductDTO[];
};

export type UpdateRecipeRequest = {
  id: number;
  recipeName: string;
  preparation: string;
  preparationTime: number;
  mealType: MealType;
  cuisine: string;
  portions: number;
  updateImage: boolean;
  products: RecipeProductDTO[];
};
