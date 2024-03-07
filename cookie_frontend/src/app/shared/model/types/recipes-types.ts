import { MealType } from '../enums/meal-type.enum';
import { Unit } from '../enums/unit.enum';
import { ProductDTO } from './product-types';

export type RecipeDTO = {
  id: number;
  recipeName: string;
  preparationTime: number;
  mealType: MealType;
  cuisine: string;
  portions: number;
  recipeImage: Blob;
  creatorUserName: string;
  nrOfProducts: number;
};

export type RecipeProductDTO = {
  id: number | null;
  product: ProductDTO;
  quantity: number;
  unit: Unit;
};

export type RecipeDetailsDTO = {
  id: number;
  recipeName: string;
  preparation: string;
  preparationTime: number;
  mealType: MealType;
  cuisine: string;
  portions: number;
  recipeImage: Blob;
  creatorName: string;
  products: RecipeProductDTO[];
};

export type GetRecipesParams = {
  filterValue: string;
  mealTypes: MealType[];
  prepTime: number;
  portions: number;
  sortColName: string;
  sortDirection: string;
};
