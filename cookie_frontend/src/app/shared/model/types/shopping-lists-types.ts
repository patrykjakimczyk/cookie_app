import { Category } from '../enums/cateory-enum';
import { Unit } from '../enums/unit.enum';
import { AuthorityDTO } from './user-types';

export type ShoppingListDTO = {
  listId: number;
  listName: string;
  nrOfProducts: number;
  nrOfPurchasedProducts: number;
  groupId: number;
  groupName: string;
  purchased: boolean;
};

export type GetUserShoppingListsResponse = {
  shoppingLists: ShoppingListDTO[];
};

export type GetShoppingListResponse = {
  id: number;
  listName: string;
  authorities: AuthorityDTO[];
  assignedPantry: boolean;
};

export type DeleteShoppingListResponse = {
  deletedListName: string;
};

export type CreateShoppingListRequest = {
  shoppingListName: string;
  groupId: number;
};

export type UpdateShoppingListRequest = {
  shoppingListName: string;
};

export type ShoppingListProductDTO = {
  id: number;
  productName: string;
  category: Category;
  quantity: number;
  unit: Unit;
  purchased: boolean;
};

export type EditShoppingListProductInfo = {
  listId: number;
  listProduct: ShoppingListProductDTO;
  isShoppingListProduct: boolean;
};
