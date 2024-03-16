import { Unit } from '../enums/unit.enum';
import { GetPantryResponse } from '../responses/pantry-response';
import { ProductDTO } from './product-types';

export type PantryDTO = {
  pantryId: number;
  pantryName: string;
  nrOfProducts: number;
  groupId: number;
  groupName: string;
};

export type PantryProductDTO = {
  id: number | null;
  product: ProductDTO;
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

export type AddToListInfo = {
  pantryProduct: PantryProductDTO;
  pantry: GetPantryResponse;
};
