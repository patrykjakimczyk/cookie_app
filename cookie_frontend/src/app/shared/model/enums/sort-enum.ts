export enum SortColumnName {
  QUANTITY = 'quantity',
  PLACEMENT = 'placement',
  EXPIRATION_DATE = 'expiration_date',
}

export const sortColumnNames = [
  { name: 'Quantity', value: SortColumnName.QUANTITY },
  { name: 'Placement', value: SortColumnName.PLACEMENT },
  { name: 'Expiration date', value: SortColumnName.EXPIRATION_DATE },
];

export enum SortDirection {
  DESCENDING = 'DESC',
  ASCENDING = 'ASC',
}

export const sortDirecitons = [
  { name: 'Descending', value: SortDirection.DESCENDING },
  { name: 'Ascending', value: SortDirection.ASCENDING },
];
