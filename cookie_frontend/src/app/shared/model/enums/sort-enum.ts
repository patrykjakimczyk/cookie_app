export enum SortColumnName {
  QUANTITY = 'quantity',
  PLACEMENT = 'placement',
  EXPIRATION_DATE = 'expiration_date',
  RECIPE_NAME = 'recipe_name',
  PREPARATION_TIME = 'preparation_time',
  CUISINE = 'cuisine',
  PORTIONS = 'portions',
}

export const pantrySortColumnNames = [
  { name: 'Quantity', value: SortColumnName.QUANTITY },
  { name: 'Placement', value: SortColumnName.PLACEMENT },
  { name: 'Expiration date', value: SortColumnName.EXPIRATION_DATE },
];

export const recipeSortColumnNames = [
  { name: 'Recipe name', value: SortColumnName.RECIPE_NAME },
  { name: 'Preparation time', value: SortColumnName.PREPARATION_TIME },
  { name: 'Cuisine', value: SortColumnName.CUISINE },
  { name: 'Portions', value: SortColumnName.PORTIONS },
];

export const shoppingListSortColumnNames = [
  { name: 'Quantity', value: SortColumnName.QUANTITY },
];

export enum SortDirection {
  DESCENDING = 'DESC',
  ASCENDING = 'ASC',
}

export const sortDirecitons = [
  { name: 'Descending', value: SortDirection.DESCENDING },
  { name: 'Ascending', value: SortDirection.ASCENDING },
];
