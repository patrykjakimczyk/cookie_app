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

export type GetRecipesParams = {
  filterValue: string;
  prepTime: number;
  portions: number;
  sortColName: string;
  sortDirection: string;
};
