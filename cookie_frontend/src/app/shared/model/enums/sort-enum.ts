export enum SortColumnName {
  PURCHASE_DATE = 'purchase_date',
  EXPIRATION_DATE = 'expiration_date',
  PLACEMENT = 'placement',
}

export const sortColumnNames = [
  { name: 'Purchase date', value: SortColumnName.PURCHASE_DATE },
  { name: 'Expiration date', value: SortColumnName.EXPIRATION_DATE },
  { name: 'Placement', value: SortColumnName.PLACEMENT },
];

export enum SortDirection {
  DESCENDING = 'DESC',
  ASCENDING = 'ASC',
}

export const sortDirecitons = [
  { name: 'Descending', value: SortDirection.DESCENDING },
  { name: 'Ascending', value: SortDirection.ASCENDING },
];
