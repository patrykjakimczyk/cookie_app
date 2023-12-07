import { Unit } from '../enums/unit.enum';
import { Category } from '../enums/cateory-enum';

export type ProductDTO = {
  productName: string;
  category: Category;
};

export type PantryProductDTO = {
  id: number | null;
  productName: string;
  category: string;
  quantity: number;
  unit: Unit;
  reserved: number;
  purchaseDate: string;
  expirationDate: string;
  placement: string;
};

export type ReserveType = 'RESERVE' | 'UNRESERVE';

export type ReservePantryProductInfo = {
  pantryId: number;
  pantryProduct: PantryProductDTO;
  reserve: ReserveType;
};

export type EditPantryInfo = {
  pantryId: number;
  pantryProduct: PantryProductDTO;
  isPantryProduct: boolean;
};