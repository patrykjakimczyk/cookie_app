import { Unit } from '../enums/unit.enum';

export type RecipeDTO = {
  id: number;
  recipeName: string;
  preparationTime: number;
  cuisine: string;
  portions: number;
  recipeImage: Uint8Array;
  creatorUserName: string;
  nrOfProducts: number;
};

export type RecipeProductDTO = {
  id: number | null;
  productName: string;
  category: string;
  quantity: number;
  unit: Unit;
};

export type RecipeDetailsDTO = {
  id: number;
  recipeName: string;
  preparation: string;
  preparationTime: number;
  cuisine: string;
  portions: number;
  recipeImage: Uint8Array;
  creatorName: string;
  products: RecipeProductDTO[];
};

export type GetRecipesParams = {
  filterValue: string;
  prepTime: number;
  portions: number;
  sortColName: string;
  sortDirection: string;
};
