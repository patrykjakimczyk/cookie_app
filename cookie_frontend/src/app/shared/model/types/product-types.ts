import { Category } from '../enums/category.enum';

export type ProductDTO = {
  productId: number | null;
  productName: string;
  category: Category;
};
