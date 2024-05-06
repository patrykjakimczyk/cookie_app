export type PageResult<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  pageNr: number;
};
