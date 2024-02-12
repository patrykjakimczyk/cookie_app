import { MealType } from '../enums/meal-type.enum';
import { RecipeProductDTO } from '../types/recipes-types';

export type CreateRecipeRequest = {
  id: number;
  recipeName: string;
  preparation: string;
  preparationTime: number;
  mealType: MealType;
  cuisine: string;
  portions: number;
  creatorName: string;
  updateImage: boolean;
  products: RecipeProductDTO[];
};
